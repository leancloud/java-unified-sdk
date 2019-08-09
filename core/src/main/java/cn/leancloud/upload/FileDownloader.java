package cn.leancloud.upload;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.core.PaasClient;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class FileDownloader {
  private static final AVLogger gLogger = LogUtil.getLogger(FileDownloader.class);

  private static final int READ_BUF_SIZE = 1024*8;

  public AVException execute(final String url, File localFile) {
    if (StringUtil.isEmpty(url)) {
      return new AVException(new IllegalArgumentException("url is null"));
    }
    if (localFile.exists()) {
      return null;
    }
    return downloadFileFromNetwork(url, localFile);
  }

  private OkHttpClient getHttpClient() {
    return new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
  }

  private AVException downloadFileFromNetwork(final String url, File cacheFile) {

    AVException errors = null;
    Request.Builder requestBuilder = new Request.Builder();
    requestBuilder.url(url);

    OkHttpClient client = getHttpClient();
    try {
      Response response = client.newCall(requestBuilder.build()).execute();
      int statusCode = response.code();
      Headers headers = response.headers();
      InputStream data = response.body().byteStream();
      if (statusCode / 100 == 2 && null != data) {
        // read data from InputStream and save to cache File
        byte[] content = new byte[READ_BUF_SIZE];

        FileOutputStream out = null;
        Lock writeLock = PersistenceUtil.sharedInstance().getLock(cacheFile.getAbsolutePath()).writeLock();
        if (writeLock.tryLock()) {
          try {
            out = new FileOutputStream(cacheFile, false);
            int currentReadSize = data.read(content);
            while (currentReadSize > 0) {
              out.write(content, 0, currentReadSize);
              currentReadSize = data.read(content);
            }
          } catch (Exception e) {
            gLogger.w(e);
            errors = new AVException(e);
          } finally {
            try {
              data.close();
            } catch (IOException e) {
            }
            if (out != null) {
              try {
                out.close();
              } catch (IOException e) {
              }
            }
            writeLock.unlock();
          }
        } else {
          gLogger.w("failed to lock writeLocker, skip to save network streaming to local cache.");
        }
      } else if (null != data) {
        errors = new AVException(statusCode, "status code is invalid");
        gLogger.w(errors);
      } else {
        errors = new AVException(statusCode, "data is empty!");
        gLogger.w(errors);
      }
    } catch (IOException ex) {
      errors = new AVException(ex);
    }
    return errors;
  }
}
