package cn.leancloud.upload;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.ProgressCallback;
import cn.leancloud.core.AVFile;
import cn.leancloud.network.DNSDetoxicant;
import cn.leancloud.network.LoggingInterceptor;
import cn.leancloud.network.PaasClient;
import cn.leancloud.network.RequestPaddingInterceptor;
import cn.leancloud.utils.LogUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class HttpClientUploader implements Uploader {
  private static AVLogger LOGGER = LogUtil.getLogger(HttpClientUploader.class);

  ProgressCallback progressCallback;

  private volatile boolean cancelled = false;
//  static ThreadPoolExecutor executor;

//  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
//  private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
//  private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
//  private static final long KEEP_ALIVE_TIME = 1L;
  protected static final int DEFAULT_RETRY_TIMES = 6;

//  static {
//    executor = new ThreadPoolExecutor(
//            CORE_POOL_SIZE,
//            MAX_POOL_SIZE,
//            KEEP_ALIVE_TIME, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<Runnable>());
//  }

  protected static synchronized OkHttpClient getOKHttpClient() {
    return new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .dns(new DNSDetoxicant())
            .build();
  }

  protected AVFile avFile = null;

  public HttpClientUploader(AVFile file, ProgressCallback progressCallback) {
    this.avFile = file;
    this.progressCallback = progressCallback;
    cancelled = false;
  }

  protected Response executeWithRetry(Request request, int retry) throws AVException {
    if (retry > 0 && !isCancelled()) {
      try {
        Response response = getOKHttpClient().newCall(request).execute();
        if (response.code() / 100 == 2) {
          return response;
        } else {
          return executeWithRetry(request, retry - 1);
        }
      } catch (IOException e) {
        return executeWithRetry(request, retry - 1);
      }
    } else {
      throw new AVException(AVException.OTHER_CAUSE, "Upload File failure");
    }
  }

  public void publishProgress(int progress) {
    if (progressCallback != null) progressCallback.internalDone(progress, null);
  }

  // ignore interrupt so far.
  public boolean cancel(boolean interrupt) {
    if (cancelled) {
      return false;
    }
    cancelled = true;
    if (interrupt) {
      interruptImmediately();
    }
    return true;
  }

  public void interruptImmediately() {
  }

  public boolean isCancelled() {
    return cancelled;
  }
}
