package cn.leancloud.upload;

import cn.leancloud.AVException;
import cn.leancloud.callback.ProgressCallback;
import cn.leancloud.AVFile;
import cn.leancloud.utils.FileUtil;
import cn.leancloud.utils.StringUtil;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class S3Uploader extends HttpClientUploader {
  private static String DEFAULT_HEADER_CACHE_CONTROL = "Cache-Control";
  private static String DEFAULT_HEADER_CACHE_CONTROL_VALUE = "public, max-age=31536000";

  private volatile Call call;
  private String uploadUrl;
  private int retryTimes = DEFAULT_RETRY_TIMES;

  /**
   * 默认的最小写操作时间，单位为秒
   */
  private static final int DEFAULT_MIN_WRITE_TIMEOUT = 30;

  /**
   * 默认的最大写操作时间，单位为秒
   */
  private static final int DEFAULT_MAX_WRITE_TIMEOUT = 4 * 60;

  /**
   * 默认最小的上传速率，50kb 每秒
   */
  private static final int DEFAULT_MIN_UPLOAD_RATE = 50 * 1024;

  /**
   * 写操作时间
   */
  private static int writeTimeout = 0;

  S3Uploader(AVFile avFile, String uploadUrl, ProgressCallback progressCallback) {
    super(avFile, progressCallback);
    this.uploadUrl = uploadUrl;
  }

  public AVException execute() {
    try {
      byte[] bytes = avFile.getData();

      return executeWithRetry(bytes);
    } catch (Exception e) {
      return new AVException(e.getCause());
    }
  }

  private AVException executeWithRetry(byte[] data){
    if (null != data && data.length > 0) {
      OkHttpClient.Builder okhttpBuilder = getOKHttpClient().newBuilder();

      int timeout = (writeTimeout > 0 ? writeTimeout : getWriteTimeoutByLength(data.length));
      okhttpBuilder.writeTimeout(timeout, TimeUnit.SECONDS);

      final OkHttpClient httpClient = okhttpBuilder.build();

      Response response = null;
      String serverResponse = null;
      try{
        // decide file mimetype.
        String mimeType = FileUtil.getFileMimeType(avFile);

        // upload to s3
        Request.Builder builder = new Request.Builder();
        builder.url(uploadUrl);
        // ================================================================================
        // setup multi part
        // ================================================================================

        // support file for future

        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), data);

        builder.put(requestBody);
        builder.addHeader("Content-Type", mimeType);
        if (!FileUploader.UPLOAD_HEADERS.containsKey(DEFAULT_HEADER_CACHE_CONTROL)) {
          builder.addHeader(DEFAULT_HEADER_CACHE_CONTROL, DEFAULT_HEADER_CACHE_CONTROL_VALUE);
        }
        for (Map.Entry<String, String> entry : FileUploader.UPLOAD_HEADERS.entrySet()) {
          builder.addHeader(entry.getKey(), entry.getValue());
        }

        // Send it
        call = httpClient.newCall(builder.build());

        response = call.execute();
        // The 204 status code implies no response is needed
        if (2 != (response.code() / 100)) {
          if(retryTimes>0){
            retryTimes -- ;
            executeWithRetry(data);
          }else {
            return new AVException(AVException.OTHER_CAUSE, "upload file failure:" + response.code());
          }
        }
      }catch (IOException exception){
        if(retryTimes >0){
          retryTimes -- ;
          return executeWithRetry(data);
        }else {
          return new AVException(exception.getCause());
        }
      }
    }
    return null;
  }

  /**
   * 根据文件长度获取写操作超时时间，默认按照 50kb 的速率计算
   * 最小不超过 30s，最大不超过 240s
   * @param dataLength
   * @return
   */
  private int getWriteTimeoutByLength(int dataLength) {
    int writeSecond = dataLength / DEFAULT_MIN_UPLOAD_RATE;
    if (writeSecond < DEFAULT_MIN_WRITE_TIMEOUT) {
      writeSecond = DEFAULT_MIN_WRITE_TIMEOUT;
    } else if (writeSecond > DEFAULT_MAX_WRITE_TIMEOUT) {
      writeSecond = DEFAULT_MAX_WRITE_TIMEOUT;
    }
    return writeSecond;
  }

  /**
   * Sets the write timeout for s3
   * @param seconds
   * @throws AVException
   */
  public static void setWriteTimeout(int seconds) throws AVException {
    if (seconds <= 0) {
      throw new AVException(new IllegalArgumentException("Timeout too small"));
    }
    if (seconds > 60 * 60) {
      throw new AVException(new IllegalArgumentException("Timeout too large"));
    }
    writeTimeout = seconds;
  }
}