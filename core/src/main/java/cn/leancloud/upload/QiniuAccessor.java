package cn.leancloud.upload;


import cn.leancloud.LCException;
import cn.leancloud.LCLogger;
import cn.leancloud.codec.Base64;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.List;
import java.util.zip.CRC32;

/**
 * Qiniu REST API client for file uploading.
 * 1. slicing upload:
 * following is the architecture of a file which is composed by block and chunk.
 * ref: https://developer.qiniu.com/kodo/manual/1650/chunked-upload
 * |----------------------------------------------------------------------------|
 * | resource                                                                   |
 * |   |-------------|   |-------------|   |-------------|   |-------------|    |
 * |   | block1      |   | block2      |   | block3      |   | block4      |    |
 * |   | |---------| |   | |---------| |   | |---------| |   | |---------| |    |
 * |   | | chunk1  | |   | | chunk1  | |   | | chunk1  | |   | | chunk1  | |    |
 * |   | |---------| |   | |---------| |   | |---------| |   | |---------| |    |
 * |   | | chunk2  | |   | | chunk2  | |   | | chunk2  | |   | | chunk2  | |    |
 * |   | |---------| |   | |---------| |   | |---------| |   | |---------| |    |
 * |   | | chunk3  | |   | | chunk3  | |   | | chunk3  | |   | | chunk3  | |    |
 * |   | |---------| |   | |---------| |   | |---------| |   | |---------| |    |
 * |   | | chunk4  | |   | | chunk4  | |   | | chunk4  | |   | | chunk4  | |    |
 * |   | |---------| |   | |---------| |   | |---------| |   | |---------| |    |
 * |   |-------------|   |-------------|   |-------------|   |-------------|    |
 * |----------------------------------------------------------------------------|
 *
 * 2. main steps
 *   - 2.1 make block. ref: https://developer.qiniu.com/kodo/api/1286/mkblk
 *   - 2.2 upload chunks. ref: https://developer.qiniu.com/kodo/api/1251/bput
 *   - 2.3 merge to file. ref: https://developer.qiniu.com/kodo/api/1287/mkfile
 *
 * Created by fengjunwen on 2017/8/15.
 */

class QiniuAccessor {
  private static LCLogger LOGGER = LogUtil.getLogger(QiniuAccessor.class);

  static final String QINIU_HOST = "http://upload.qiniu.com";
  static final String QINIU_CREATE_BLOCK_EP = "%s/mkblk/%d";
  static final String QINIU_BRICK_UPLOAD_EP = "%s/bput/%s/%d";
  static final String QINIU_MKFILE_EP = "%s/mkfile/%d/key/%s";

  static final String HEAD_CONTENT_LENGTH = "Content-Length";
  static final String HEAD_CONTENT_TYPE = "Content-Type";
  static final String HEAD_AUTHORIZATION = "Authorization";
  static final String TEXT_CONTENT_TYPE = "text/plain";
  static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

  static final int WIFI_CHUNK_SIZE = 256 * 1024;
  static final int BLOCK_SIZE = 1024 * 1024 * 4;
  static final int NONWIFI_CHUNK_SIZE = 64 * 1024;

  static class QiniuBlockResponseData {
    private String ctx;
    private long crc32;
    private int offset;
    private String host;
    private String checksum;

    public String getCtx() {
      return ctx;
    }

    public void setCtx(String ctx) {
      this.ctx = ctx;
    }

    public long getCrc32() {
      return crc32;
    }

    public void setCrc32(long crc32) {
      this.crc32 = crc32;
    }

    public int getOffset() {
      return offset;
    }

    public void setOffset(int offset) {
      this.offset = offset;
    }

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public String getChecksum() {
      return checksum;
    }

    public void setChecksum(String checksum) {
      this.checksum = checksum;
    }

    @Override
    public String toString() {
      return "QiniuBlockResponseData{" +
              "ctx='" + ctx + '\'' +
              ", crc32=" + crc32 +
              ", offset=" + offset +
              ", host='" + host + '\'' +
              ", checksum='" + checksum + '\'' +
              '}';
    }
  }

  static class QiniuMKFileResponseData {
    public String key;
    public String hash;

    @Override
    public String toString() {
      return "QiniuMKFileResponseData{" +
              "key='" + key + '\'' +
              ", hash='" + hash + '\'' +
              '}';
    }
  }

  private OkHttpClient client;
  private String uploadToken;
  private String fileKey;
  private String uploadUrl = QINIU_HOST;

  QiniuAccessor(OkHttpClient client, String uploadToken, String fileKey, String uploadUrl) {
    this.client = client;
    this.uploadToken = uploadToken;
    this.fileKey = fileKey;
    if (!StringUtil.isEmpty(uploadUrl)) {
      this.uploadUrl = uploadUrl;
    }
  }

  private static <T> T parseQiniuResponse(Response resp, Class<T> clazz) throws Exception {

    int code = resp.code();
    String phrase = resp.message();

    String h = resp.header("X-Log");

    if (code == 401) {
      throw new Exception("unauthorized to create Qiniu Block");
    }
    String responseData = StringUtil.stringFromBytes(resp.body().bytes());
    try {
      if (code / 100 == 2) {
        T data = JSON.parseObject(responseData, clazz);
        return data;
      }
    } catch (Exception e) {
      LOGGER.w(e);
    }

    if (responseData.length() > 0) {
      throw new Exception(code + ":" + responseData);
    }
    if (!StringUtil.isEmpty(h)) {
      throw new Exception(h);
    }
    throw new Exception(phrase);
  }

  /**
   * REST API:
   * - POST /mkblk/<blockSize> HTTP/1.1
   * - Host:           upload.qiniu.com
   * - Content-Type:   application/octet-stream
   * - Content-Length: <firstChunkSize>
   * - Authorization:  UpToken <UploadToken>
   * - <firstChunkBinary>
   *
   * - Response
   * {
   *   "ctx":          "<Ctx           string>",
   *   "checksum":     "<Checksum      string>",
   *   "crc32":         <Crc32         int64>,
   *   "offset":        <Offset        int64>,
   *   "host":         "<UpHost        string>"
   * }
   * @param blockSize
   * @param firstChunkSize
   * @param firstChunkData
   * @param retry
   * @return
   */
  public QiniuBlockResponseData createBlockInQiniu(int blockSize, int firstChunkSize,
                                                   final byte[] firstChunkData, int retry) {
    try {

      String endPoint = String.format(QINIU_CREATE_BLOCK_EP, this.uploadUrl, blockSize);
      Request.Builder builder = new Request.Builder();
      builder.url(endPoint);
      builder.addHeader(HEAD_CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
      builder.addHeader(HEAD_CONTENT_LENGTH, String.valueOf(firstChunkSize));
      builder.addHeader(HEAD_AUTHORIZATION, "UpToken " + this.uploadToken);

      LOGGER.d("createBlockInQiniu with uploadUrl: " + endPoint);
      RequestBody requestBody = RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), firstChunkData, 0, firstChunkSize);
      builder = builder.post(requestBody);

      Response response = this.client.newCall(builder.build()).execute();
      return parseQiniuResponse(response, QiniuBlockResponseData.class);
    } catch (Exception e) {
      if (retry-- > 0) {
        return createBlockInQiniu(blockSize, firstChunkSize, firstChunkData, retry);
      } else {
        LOGGER.w(e);
      }
    }
    return null;
  }

  /**
   * REST API:
   * POST /bput/<ctx>/<nextChunkOffset> HTTP/1.1
   * Host:           <UpHost>
   * Content-Type:   application/octet-stream
   * Content-Length: <nextChunkSize>
   * Authorization:  UpToken <UploadToken>
   * <nextChunkBinary>
   *
   * Request Params:
   * - ctx: 前一次上传返回的块级上传控制信息。
   * - nextChunkOffset: 当前片在整个块中的起始偏移。
   * - nextChunkSize: 当前片数据大小
   * - nextChunkBinary: 当前片数据
   *
   * Response:
   * {
   *   "ctx":            "<Ctx          string>",
   *   "checksum":       "<Checksum     string>",
   *   "crc32":           <Crc32        int64>,
   *   "offset":          <Offset       int64>,
   *   "host":           "<UpHost       string>"
   * }
   *
   * @param lastChunk
   * @param blockOffset
   * @param currentChunkData
   * @param currentChunkSize
   * @param retry
   * @return
   */
  public QiniuBlockResponseData putFileBlocksToQiniu(QiniuBlockResponseData lastChunk,
                                                     final int blockOffset,
                                                     final byte[] currentChunkData,
                                                     int currentChunkSize, int retry) {
    try {
      String endPoint = String.format(QINIU_BRICK_UPLOAD_EP, this.uploadUrl, lastChunk.ctx, lastChunk.offset);
      Request.Builder builder = new Request.Builder();
      builder.url(endPoint);
      builder.addHeader(HEAD_CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
      builder.addHeader(HEAD_CONTENT_LENGTH, String.valueOf(currentChunkSize));
      builder.addHeader(HEAD_AUTHORIZATION, "UpToken " + this.uploadToken);

      LOGGER.d("putFileBlocksToQiniu with uploadUrl: " + endPoint);

      RequestBody requestBody = RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE),
              currentChunkData, 0, currentChunkSize);
      builder = builder.post(requestBody);
      Response response = this.client.newCall(builder.build()).execute();
      QiniuBlockResponseData respData = parseQiniuResponse(response, QiniuBlockResponseData.class);
      validateCrc32Value(respData, currentChunkData, 0, currentChunkSize);
      return respData;
    } catch (Exception e) {
      if (retry-- > 0) {
        return putFileBlocksToQiniu(lastChunk, blockOffset, currentChunkData, currentChunkSize, retry);
      } else {
        LOGGER.w(e);
      }
    }
    return null;
  }

  private void validateCrc32Value(QiniuBlockResponseData respData, byte[] data, int offset, int nextChunkSize)
          throws LCException {
    CRC32 crc32 = new CRC32();
    crc32.update(data,offset,nextChunkSize);
    long localCRC32 = crc32.getValue();
    if(respData!=null && respData.crc32 != localCRC32){
      throw  new LCException(LCException.OTHER_CAUSE,"CRC32 validation failure for chunk upload");
    }
  }

  /**
   * REST API
   * POST /mkfile/<fileSize>/key/<encodedKey>/mimeType/<encodedMimeType>/x:user-var/<encodedUserVars> HTTP/1.1
   * Host:           <UpHost>
   * Content-Type:   text/plain
   * Content-Length: <ctxListSize>
   * Authorization:  UpToken <UploadToken>
   * <ctxList>
   *
   * Request params:
   * - fileSize
   * - encodeKey
   * - encodedMimeType
   * - encodedUserVars
   *
   * Response:
   * {
   *   "hash": "<ContentHash  string>",
   *   "key":  "<Key          string>"
   * }
   *
   * @param fileTotalSize
   * @param uploadFileCtxs
   * @param retry
   * @return
   * @throws Exception
   */
  public QiniuMKFileResponseData makeFile(int fileTotalSize, List<String> uploadFileCtxs, int retry)
          throws Exception {
    try {
      String endPoint = String.format(QINIU_MKFILE_EP, this.uploadUrl, fileTotalSize,
              Base64.encodeToString(this.fileKey.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));
      final String joinedFileCtx = StringUtil.join(",", uploadFileCtxs);
      Request.Builder builder = new Request.Builder();
      builder.url(endPoint);
      builder.addHeader(HEAD_CONTENT_TYPE, TEXT_CONTENT_TYPE);
      builder.addHeader(HEAD_CONTENT_LENGTH, String.valueOf(joinedFileCtx.length()));
      builder.addHeader(HEAD_AUTHORIZATION, "UpToken " + this.uploadToken);

      LOGGER.d("makeFile to qiniu with uploadUrl: " + endPoint);
      builder = builder.post(RequestBody.create(MediaType.parse(TEXT_CONTENT_TYPE), joinedFileCtx));
      Response response = this.client.newCall(builder.build()).execute();
      return parseQiniuResponse(response, QiniuMKFileResponseData.class);
    } catch (Exception e) {
      if (retry-- > 0) {
        return makeFile(fileTotalSize, uploadFileCtxs, retry);
      } else {
        LOGGER.w(e);
      }
    }
    return null;
  }
}

