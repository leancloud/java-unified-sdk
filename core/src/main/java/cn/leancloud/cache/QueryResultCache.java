package cn.leancloud.cache;

import cn.leancloud.AVObject;
import cn.leancloud.query.AVQueryResult;
import io.reactivex.Observable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class QueryResultCache extends LocalStorage {
  private static QueryResultCache INSTANCE = null;

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
    return sb.toString();
  }

  public Observable<List<AVObject>> getCacheResult(final String className, final Map<String, String> query, final long maxAgeInMilliseconds) {
    Callable<List<AVObject>> callable = new Callable<List<AVObject>>() {
      public List<AVObject> call() throws Exception {
        String cacheKey = generateKeyForQueryCondition(className, query);
        File cacheFile = getCacheFile(cacheKey);
        if (null == cacheFile || !cacheFile.exists()) {
          throw new FileNotFoundException("cache is not existed.");
        }
        if (maxAgeInMilliseconds > 0 && (System.currentTimeMillis() - cacheFile.lastModified() > maxAgeInMilliseconds)) {
          throw new FileNotFoundException("cache file is expired.");
        }
        byte[] content = readData(cacheFile);
        if (null == content) {
          throw new InterruptedException("failed to read cache file.");
        }
        AVQueryResult result = AVQueryResult.fromJSONString(String.valueOf(content));
        return result.getResults();
      }
    };
    FutureTask<List<AVObject>> futureTask = new FutureTask<List<AVObject>>(callable);
    return Observable.fromFuture(futureTask);
  }
}
