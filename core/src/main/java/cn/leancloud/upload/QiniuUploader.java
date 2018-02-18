package cn.leancloud.upload;

import cn.leancloud.AVException;
import cn.leancloud.ProgressCallback;
import cn.leancloud.SaveCallback;
import cn.leancloud.core.AVFile;
import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.CRC32;

class QiniuUploader extends HttpClientUploader {
  private String token;
  private String[] uploadFileCtx;
  private int blockCount;
  private String fileKey;

  static final String QINIU_HOST = "http://upload.qiniu.com";
  static final String QINIU_CREATE_BLOCK_EP = QINIU_HOST + "/mkblk/%d";
  static final String QINIU_BRICK_UPLOAD_EP = QINIU_HOST + "/bput/%s/%d";
  static final String QINIU_MKFILE_EP = QINIU_HOST + "/mkfile/%d/key/%s";
  static final int WIFI_CHUNK_SIZE = 256 * 1024;
  static final int BLOCK_SIZE = 1024 * 1024 * 4;
  static final int NONWIFI_CHUNK_SIZE = 64 * 1024;

  private FileUploader.ProgressCalculator progressCalculator;
  private volatile Call mergeFileRequestCall;
  private volatile Future[] tasks;

  QiniuUploader(AVFile avFile, String token, String fileKey, SaveCallback saveCallback, ProgressCallback progressCallback) {
    super(avFile, saveCallback,progressCallback);

    this.token = token;
    this.fileKey = fileKey;
  }

  int uploadChunkSize = WIFI_CHUNK_SIZE;
  // // FIXME: 2017/8/14 why not use executorService declared in parent class(HttpClientUploader.ThreadPoolExecutor)??
  static final ExecutorService fileUploadExecutor = Executors.newFixedThreadPool(10);

  @Override
  public AVException doWork() {
    boolean isWifi = AVUtils.isWifi(AVOSCloud.applicationContext);
    if (!isWifi) {
      // 从七牛的接口来看block size为4M不可变，但是chunkSize是可以调整的
      uploadChunkSize = NONWIFI_CHUNK_SIZE;
    }
    if (AVOSCloud.isDebugLogEnabled()) {
      LogUtil.avlog.d("uploading with chunk size:" + uploadChunkSize);
    }
    // here to try
    return uploadWithBlocks();

  }

  private Request.Builder addAuthHeader(Request.Builder builder) throws Exception {
    if (token != null) {
      builder.addHeader("Authorization", "UpToken " + token);
    }
    return builder;
  }

  private AVException uploadWithBlocks() {

    try {
      byte[] bytes = avFile.getData();
      blockCount = (bytes.length / BLOCK_SIZE) + (bytes.length % BLOCK_SIZE > 0 ? 1 : 0);
      uploadFileCtx = new String[blockCount];

      // 2.按照分片进行上传
      QiniuBlockResponseData respBlockData = null;
      CountDownLatch latch = new CountDownLatch(blockCount);
      progressCalculator = new FileUploader.ProgressCalculator(blockCount, new FileUploader.FileUploadProgressCallback() {
        public void onProgress(int progress) {
          publishProgress(progress);
        }
      });
      tasks = new Future[blockCount];
      synchronized (tasks) {
        for (int blockOffset = 0; blockOffset < blockCount; blockOffset++) {
          tasks[blockOffset] =
                  fileUploadExecutor.submit(new FileBlockUploadTask(bytes, blockOffset, latch,
                          uploadChunkSize, progressCalculator, uploadFileCtx, this));
        }
      }
      latch.await();
      if (AVExceptionHolder.exists()) {
        for (Future task : tasks) {
          if (!task.isDone()) {
            task.cancel(true);
          }
        }

        throw AVExceptionHolder.remove();
      }
      // 3 merge文件
      QiniuMKFileResponseData mkfileResp = makeFile(bytes.length, fileKey, DEFAULT_RETRY_TIMES);

      if (!isCancelled()) {
        // qiniu's status code is 200, but should be 201 like parse..
        if (mkfileResp == null || !mkfileResp.key.equals(fileKey)) {
          return AVErrorUtils.createException(AVException.OTHER_CAUSE, "upload file failure");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new AVException(e);
    }
    return null;
  }

  private static class QiniuBlockResponseData {
    public String ctx;
    public long crc32;
    public int offset;
    public String host;
    public String checksum;
  }

  private static class QiniuMKFileResponseData {
    public String key;
    public String hash;
  }

  private QiniuMKFileResponseData makeFile(int dataSize, String key, int retry) throws Exception {
    try {
      String endPoint = String.format(QINIU_MKFILE_EP, dataSize, AVUtils.base64Encode(key));
      List<String> list = new LinkedList<String>();
      Collections.addAll(list, uploadFileCtx);
      final String joinedFileCtx = AVUtils.joinCollection(list, ",");
      Request.Builder builder = new Request.Builder();
      builder.url(endPoint);

      builder = builder.post(RequestBody.create(MediaType.parse("text"), joinedFileCtx));
      builder = addAuthHeader(builder);
      mergeFileRequestCall = getOKHttpClient().newCall(builder.build());
      return parseQiniuResponse(mergeFileRequestCall.execute(), QiniuMKFileResponseData.class);
    } catch (Exception e) {
      if (retry-- > 0) {
        return makeFile(dataSize, key, retry);
      } else {
        LogUtil.log.e("Exception during file upload", e);
      }
    }
    return null;
  }

  private static <T> T parseQiniuResponse(Response resp, Class<T> clazz) throws Exception {

    int code = resp.code();
    String phrase = resp.message();

    String h = resp.header("X-Log");

    if (code == 401) {
      throw new Exception("unauthorized to create Qiniu Block");
    }
    String responseData = AVUtils.stringFromBytes(resp.body().bytes());
    try {
      if (code / 100 == 2) {
        T data = JSON.parseObject(responseData, clazz);
        return data;
      }
    } catch (Exception e) {
    }

    if (responseData.length() > 0) {
      throw new Exception(code + ":" + responseData);
    }
    if (!AVUtils.isBlankString(h)) {
      throw new Exception(h);
    }
    throw new Exception(phrase);
  }

  private static class FileBlockUploadTask implements Runnable {
    private byte[] bytes;
    private int blockOffset;
    CountDownLatch latch;
    final int uploadChunkSize;
    ProgressCalculator progressCalculator;
    String[] uploadFileCtx;
    QiniuUploader parent;

    public FileBlockUploadTask(byte[] bytes, int blockOffset, CountDownLatch latch,
                               int uploadChunkSize, ProgressCalculator progressCalculator, String[] uploadFileCtx,
                               QiniuUploader parent) {
      this.bytes = bytes;
      this.blockOffset = blockOffset;
      this.latch = latch;
      this.uploadChunkSize = uploadChunkSize;
      this.progressCalculator = progressCalculator;
      this.uploadFileCtx = uploadFileCtx;
      this.parent = parent;
    }

    public void run() {
      QiniuBlockResponseData respBlockData;
      // 1.创建一个block,并且会上传第一个block的第一个chunk的数据
      int currentBlockSize =
              getCurrentBlockSize(bytes, blockOffset);
      respBlockData =
              createBlockInQiniu(blockOffset, currentBlockSize, DEFAULT_RETRY_TIMES, bytes);
      // 2.分片上传
      if (respBlockData != null) {
        respBlockData =
                putFileBlocksToQiniu(blockOffset, bytes, respBlockData, DEFAULT_RETRY_TIMES);
      }
      if (respBlockData != null) {
        uploadFileCtx[blockOffset] = respBlockData.ctx;
        progressCalculator.publishProgress(blockOffset, 100);
      } else {
        AVExceptionHolder.add(new AVException(AVException.OTHER_CAUSE, "Upload File failure"));
        long count = latch.getCount();
        for (; count > 0; count--) {
          latch.countDown();
        }
      }
      latch.countDown();
    }

    private QiniuBlockResponseData createBlockInQiniu(final int blockOffset, int blockSize,
                                                      int retry, final byte[] data) {
      try {
        if (AVOSCloud.isDebugLogEnabled()) {
          LogUtil.avlog.d("try to mkblk");
        }
        String endPoint = String.format(QINIU_CREATE_BLOCK_EP, blockSize);
        Request.Builder builder = new Request.Builder();
        builder.url(endPoint);

        final int nextChunkSize =
                getNextChunkSize(blockOffset, data);

        RequestBody requestBody = RequestBody.create(MediaType.parse(AVFile.DEFAULTMIMETYPE),
                data, blockOffset * BLOCK_SIZE, nextChunkSize);

        builder = builder.post(requestBody);
        builder = parent.addAuthHeader(builder);
        return parseQiniuResponse(getOKHttpClient().newCall(builder.build()).execute(),
                QiniuBlockResponseData.class);
      } catch (Exception e) {
        e.printStackTrace();
        if (retry-- > 0) {
          return createBlockInQiniu(blockOffset, blockSize, retry, data);
        } else {
          LogUtil.log.e("Exception during file upload", e);
        }
      }
      return null;
    }


    private QiniuBlockResponseData putFileBlocksToQiniu(final int blockOffset, final byte[] data,
                                                        QiniuBlockResponseData lastChunk, int retry) {
      int currentBlockLength = getCurrentBlockSize(data, blockOffset);
      progressCalculator.publishProgress(blockOffset, 100 * lastChunk.offset / BLOCK_SIZE);

      int remainingBlockLength = currentBlockLength - lastChunk.offset;

      if (remainingBlockLength > 0 && lastChunk.offset > 0) {
        try {
          String endPoint = String.format(QINIU_BRICK_UPLOAD_EP, lastChunk.ctx, lastChunk.offset);
          Request.Builder builder = new Request.Builder();
          builder.url(endPoint);
          builder.addHeader("Content-Type", "application/octet-stream");

          final QiniuBlockResponseData chunkData = lastChunk;
          final int nextChunkSize =
                  remainingBlockLength > uploadChunkSize ? uploadChunkSize : remainingBlockLength;

          RequestBody requestBody = RequestBody.create(MediaType.parse(AVFile.DEFAULTMIMETYPE),
                  data,
                  blockOffset * BLOCK_SIZE + chunkData.offset,
                  nextChunkSize);

          builder = builder.post(requestBody);
          builder = parent.addAuthHeader(builder);
          QiniuBlockResponseData respData =
                  parseQiniuResponse(getOKHttpClient().newCall(builder.build()).execute(),
                          QiniuBlockResponseData.class);
          validateCrc32Value(respData,data,blockOffset * BLOCK_SIZE + chunkData.offset,nextChunkSize);
          if (respData != null) {
            if (respData.offset < currentBlockLength) {
              return putFileBlocksToQiniu(blockOffset, data, respData, DEFAULT_RETRY_TIMES);
            } else {
              return respData;
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          if (retry-- > 0) {
            return putFileBlocksToQiniu(blockOffset, data, lastChunk, retry);
          } else {
            LogUtil.log.e("Exception during file upload", e);
          }
        }
      } else {
        // 这个应该是遇到多余的一个block里面的数据只够本block的第一个chunk塞，这部分数据已经在mkblk的上传过了，所以直接返回原来的resp就可以了
        return lastChunk;
      }
      return null;
    }

    private void validateCrc32Value(QiniuBlockResponseData respData, byte[] data, int offset, int nextChunkSize) throws AVException {
      CRC32 crc32 = new CRC32();
      crc32.update(data,offset,nextChunkSize);
      long localCRC32 = crc32.getValue();
      if(respData!=null && respData.crc32 != localCRC32){
        throw  new AVException(AVException.OTHER_CAUSE,"CRC32 validation failure for chunk upload");
      }
    }


    private int getCurrentBlockSize(byte[] bytes, int blockOffset) {
      return (bytes.length - blockOffset * BLOCK_SIZE) > BLOCK_SIZE
              ? BLOCK_SIZE
              : (bytes.length - blockOffset * BLOCK_SIZE);
    }

    private int getNextChunkSize(int blockOffset, byte[] data) {
      return ((data.length - blockOffset * BLOCK_SIZE) > uploadChunkSize)
              ? uploadChunkSize
              : (data.length - blockOffset * BLOCK_SIZE);
    }
  }

  @Override
  public void interruptImmediately() {
    super.interruptImmediately();

    if (tasks != null && tasks.length > 0) {
      synchronized (tasks) {
        for (int index = 0; index < tasks.length; index++) {
          Future task = tasks[index];
          if (task != null && !task.isDone() && !task.isCancelled()) {
            task.cancel(true);
          }
        }
      }
    }

    if (mergeFileRequestCall != null) {
      mergeFileRequestCall.cancel();
    }
  }
}