package cn.leancloud.cache;

import cn.leancloud.AVLogger;
import cn.leancloud.AVObject;
import cn.leancloud.codec.MD5;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.query.AVQueryResult;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class QueryResultCache extends LocalStorage {
  private static final AVLogger LOGGER = LogUtil.getLogger(QueryResultCache.class);
  private static QueryResultCache INSTANCE = null;
  private ExecutorService executor = Executors.newFixedThreadPool(2);

  public static synchronized QueryResultCache getInstance() {
    if (null == INSTANCE) {
      INSTANCE = new QueryResultCache();
    }
    return INSTANCE;
  }

  private QueryResultCache() {
    super(AppConfiguration.getQueryResultCacheDir());
  }

  public String cacheResult(String key, String content) {
    LOGGER.d("save cache. key=" + key + ", value=" + content);
    if (StringUtil.isEmpty(key) || null == content) {
      return null;
    }
    try {
      return super.saveData(key, content.getBytes("UTF-8"));
    } catch (Exception ex) {
      LOGGER.w(ex);
      return null;
    }
  }

  public static String generateKeyForQueryCondition(String className, Map<String, String> query) {
    StringBuilder sb = new StringBuilder();
    sb.append(className);
    sb.append(":");
    for (Map.Entry<String, String> entry: query.entrySet()) {
      sb.append(entry.getKey());
      sb.append("=");
      sb.append(entry.getValue());
      sb.append("&");
    }
    return MD5.computeMD5(sb.toString());
  }

  public static String generateCachedKey(String className, Map<String, Object> params) {
    StringBuilder sb = new StringBuilder();
    sb.append(className);
    sb.append(":");
    for (Map.Entry<String, Object> entry: params.entrySet()) {
      sb.append(entry.getKey());
      sb.append("=");
      sb.append(entry.getValue().toString());
      sb.append("&");
    }
    return MD5.computeMD5(sb.toString());
  }

  public boolean hasCachedResult(String className, Map<String, String> query, long maxAgeInMilliseconds) {
    String cacheKey = generateKeyForQueryCondition(className, query);
    File cacheFile = getCacheFile(cacheKey);
    if (null == cacheFile || !cacheFile.exists()) {
      LOGGER.d("cache file(key=" + cacheKey + ") not existed.");
      return false;
    }
    if (maxAgeInMilliseconds > 0 && (System.currentTimeMillis() - cacheFile.lastModified() > maxAgeInMilliseconds)) {
      LOGGER.d("cache file(key=" + cacheKey + ") is expired.");
      return false;
    }
    return true;
  }

  public Observable<String> getCacheRawResult(final String className, final Map<String, String> query,
                                              final long maxAgeInMilliseconds, final boolean isFinal) {
    LOGGER.d("try to get cache raw result for class:" + className);
    String cacheKey = generateKeyForQueryCondition(className, query);
    return getCacheRawResult(className, cacheKey, maxAgeInMilliseconds, isFinal);
  }

  public Observable<String> getCacheRawResult(final String className, final String cacheKey,
                                              final long maxAgeInMilliseconds, final boolean isFinal) {
    LOGGER.d("try to get cache raw result for class:" + className);
    AppConfiguration.SchedulerCreator creator = AppConfiguration.getDefaultScheduler();
    boolean isAsync = AppConfiguration.isAsynchronized();

    Callable<String> callable = new Callable<String>() {
      public String call() throws Exception {
        File cacheFile = getCacheFile(cacheKey);
        if (null == cacheFile || !cacheFile.exists()) {
          LOGGER.d("cache file(key=" + cacheKey + ") not existed.");
          if (isFinal) {
            throw new FileNotFoundException("cache is not existed.");
          } else {
            return "";
          }
        }
        if (maxAgeInMilliseconds > 0 && (System.currentTimeMillis() - cacheFile.lastModified() > maxAgeInMilliseconds)) {
          LOGGER.d("cache file(key=" + cacheKey + ") is expired.");
          if (isFinal) {
            throw new FileNotFoundException("cache file is expired.");
          } else {
            return "";
          }
        }
        byte[] data = readData(cacheFile);
        if (null == data) {
          LOGGER.d("cache file(key=" + cacheKey + ") is empty.");
          if (isFinal) {
            throw new InterruptedException("failed to read cache file.");
          } else {
            return "";
          }
        }
        String content = new String(data, 0, data.length, "UTF-8");
        LOGGER.d("cache file(key=" + cacheKey + "), content: " + content);
        return content;
      }
    };
    FutureTask<String> futureTask = new FutureTask<>(callable);
    executor.submit(futureTask);
    Observable result = Observable.fromFuture(futureTask);
    if (isAsync) {
      result = result.subscribeOn(Schedulers.io());
    }
    if (null != creator) {
      result = result.observeOn(creator.create());
    }
    return result;
  }

  public Observable<List<AVObject>> getCacheResult(final String className, final Map<String, String> query,
                                                   final long maxAgeInMilliseconds, final boolean isFinal) {
    LOGGER.d("try to get cache result for class:" + className);
    Callable<List<AVObject>> callable = new Callable<List<AVObject>>() {
      public List<AVObject> call() throws Exception {
        String cacheKey = generateKeyForQueryCondition(className, query);
        File cacheFile = getCacheFile(cacheKey);
        if (null == cacheFile || !cacheFile.exists()) {
          LOGGER.d("cache file(key=" + cacheKey + ") not existed.");
          if (isFinal) {
            return new ArrayList<>();
          } else {
            throw new FileNotFoundException("cache is not existed.");
          }
        }
        if (maxAgeInMilliseconds > 0 && (System.currentTimeMillis() - cacheFile.lastModified() > maxAgeInMilliseconds)) {
          LOGGER.d("cache file(key=" + cacheKey + ") is expired.");
          if (isFinal) {
            return new ArrayList<>();
          } else {
            throw new FileNotFoundException("cache file is expired.");
          }
        }
        byte[] data = readData(cacheFile);
        if (null == data) {
          LOGGER.d("cache file(key=" + cacheKey + ") is empty.");
          if (isFinal) {
            return new ArrayList<>();
          } else {
            throw new InterruptedException("failed to read cache file.");
          }
        }
        String content = new String(data, 0, data.length, "UTF-8");
        LOGGER.d("cache file(key=" + cacheKey + "), content: " + content);
        AVQueryResult result = AVQueryResult.fromJSONString(content);
        return result.getResults();
      }
    };
    FutureTask<List<AVObject>> futureTask = new FutureTask<List<AVObject>>(callable);
    executor.submit(futureTask);
    return Observable.fromFuture(futureTask);
  }
}
