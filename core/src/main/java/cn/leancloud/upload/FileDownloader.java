package cn.leancloud.upload;

import cn.leancloud.LCException;
import cn.leancloud.LCLogger;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class FileDownloader {
  private static final LCLogger gLogger = LogUtil.getLogger(FileDownloader.class);

  private static final int READ_BUF_SIZE = 1024*8;

  public LCException execute(final String url, File localFile) {
    if (StringUtil.isEmpty(url)) {
      return new LCException(new IllegalArgumentException("url is null"));
    }
    if (localFile.exists()) {
      return new LCException(new FileAlreadyExistsException("local file already existed."));
    }
    return downloadFileFromNetwork(url, localFile);
  }

  private OkHttpClient createHttpClient() {
    return new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
  }

  private LCException downloadFileFromNetwork(final String url, File cacheFile) {

    LCException errors = null;
    Request.Builder requestBuilder = new Request.Builder();
    requestBuilder.url(url);

    OkHttpClient client = createHttpClient();
    Response response = null;
    try {
      response = client.newCall(requestBuilder.build()).execute();
      int statusCode = response.code();
      if (null == response.body()) {
        errors = new LCException(statusCode, "response body is invalid", statusCode);
        gLogger.w(errors);
      } else {
        InputStream data = response.body().byteStream();
        if (statusCode / 100 == 2 && null != data) {
          // read data from InputStream and save to cache File
          byte[] content = new byte[READ_BUF_SIZE];

          Lock writeLock = PersistenceUtil.sharedInstance().getLock(cacheFile.getAbsolutePath()).writeLock();
          if (writeLock.tryLock()) {
            FileOutputStream out = null;
            try {
              out = new FileOutputStream(cacheFile, false);
              int currentReadSize = data.read(content);
              while (currentReadSize > 0) {
                out.write(content, 0, currentReadSize);
                currentReadSize = data.read(content);
              }
            } catch (Exception e) {
              gLogger.w(e);
              errors = new LCException(e);
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
        } else {
          errors = new LCException(statusCode, "status code is invalid", statusCode);
          gLogger.w(errors);
        }
      }
    } catch (IOException ex) {
      errors = new LCException(ex);
    } finally {
      if (null != response) {
        response.close();
      }
    }
    return errors;
  }
}
