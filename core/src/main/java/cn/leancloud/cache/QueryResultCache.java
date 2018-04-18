package cn.leancloud.cache;

import cn.leancloud.AVLogger;
import cn.leancloud.AVObject;
import cn.leancloud.codec.MD5;
import cn.leancloud.query.AVQueryResult;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;

import java.io.File;
import java.io.FileNotFoundException;
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

  public static QueryResultCache getInstance() {
    if (null == INSTANCE) {
      synchronized(QueryResultCache.class) {
        if (null == INSTANCE) {
          INSTANCE = new QueryResultCache();
        }
      };
    }
    return INSTANCE;
  }

  private QueryResultCache() {
    super(PersistenceUtil.sharedInstance().getQueryResultCacheDir());
  }

  public String cacheResult(String key, String content) {
    LOGGER.d("save cache. key=" + key + ", value=" + content);
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

  public Observable<List<AVObject>> getCacheResult(final String className, final Map<String, String> query,
                                                   final long maxAgeInMilliseconds) {
    LOGGER.d("try to get cache result for class:" + className);
    Callable<List<AVObject>> callable = new Callable<List<AVObject>>() {
      public List<AVObject> call() throws Exception {
        String cacheKey = generateKeyForQueryCondition(className, query);
        File cacheFile = getCacheFile(cacheKey);
        if (null == cacheFile || !cacheFile.exists()) {
          LOGGER.d("cache file(key=" + cacheKey + ") not existed.");
          throw new FileNotFoundException("cache is not existed.");
        }
        if (maxAgeInMilliseconds > 0 && (System.currentTimeMillis() - cacheFile.lastModified() > maxAgeInMilliseconds)) {
          LOGGER.d("cache file(key=" + cacheKey + ") is expired.");
          throw new FileNotFoundException("cache file is expired.");
        }
        byte[] data = readData(cacheFile);
        if (null == data) {
          LOGGER.d("cache file(key=" + cacheKey + ") is empty.");
          throw new InterruptedException("failed to read cache file.");
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
