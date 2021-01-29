package cn.leancloud;

import cn.leancloud.core.PaasClient;
import cn.leancloud.query.AVCloudQueryResult;
import cn.leancloud.query.AVQueryResult;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import java.util.*;

class AVCloudQuery {
  private AVCloudQuery() {
  }

  public static Observable<AVCloudQueryResult> executeInBackground(String cql) {
    return executeInBackground(null, cql);
  }
  public static Observable<AVCloudQueryResult> executeInBackground(AVUser asAuthenticatedUser, String cql) {
    return executeInBackground(asAuthenticatedUser, cql, AVObject.class);
  }
  public static Observable<AVCloudQueryResult> executeInBackground(String cql, Object... params) {
    return executeInBackground(null, cql, params);
  }
  public static Observable<AVCloudQueryResult> executeInBackground(AVUser asAuthenticatedUser,
                                                                   String cql, Object... params) {
    return executeInBackground(asAuthenticatedUser, cql, AVObject.class, params);
  }
  public static Observable<AVCloudQueryResult> executeInBackground(String cql, Class<? extends AVObject> clazz) {
    return executeInBackground(null, cql, clazz);
  }
  public static Observable<AVCloudQueryResult> executeInBackground(AVUser asAuthenticatedUser,
                                                                   String cql, Class<? extends AVObject> clazz) {
    return executeInBackground(asAuthenticatedUser, cql, clazz, null);
  }
  public static <T extends AVObject> Observable<AVCloudQueryResult> executeInBackground(String cql, final Class<T> clazz, Object... params) {
    return executeInBackground(null, cql, clazz, params);
  }
  public static <T extends AVObject> Observable<AVCloudQueryResult> executeInBackground(AVUser asAuthenticatedUser,
                                                                                        String cql, final Class<T> clazz,
                                                                                        Object... params) {
    if (StringUtil.isEmpty(cql)) {
      throw new IllegalArgumentException("cql is empty");
    }
    if (null == clazz) {
      throw new IllegalArgumentException("target class is null");
    }
    List<Object> pValue = new LinkedList<>();
    if (null != params) {
      for (Object o: params) {
        pValue.add(o);
      }
    }
    Map<String, String> p = new HashMap<>();
    p.put("cql", cql);
    if (!pValue.isEmpty()) {
      p.put("pvalues", AVUtils.jsonStringFromObjectWithNull(pValue));
    }
    return PaasClient.getStorageClient().cloudQuery(asAuthenticatedUser, p).map(new Function<AVQueryResult, AVCloudQueryResult>() {
      public AVCloudQueryResult apply(AVQueryResult avQueryResult) throws Exception {
        AVCloudQueryResult finalResult = new AVCloudQueryResult();
        List<T> rawObjs = new ArrayList(avQueryResult.getCount());
        for (AVObject o : avQueryResult.getResults()) {
          rawObjs.add((T) Transformer.transform(o, avQueryResult.getClassName()));
        }
        finalResult.setResults(rawObjs);
        finalResult.setCount(avQueryResult.getCount());

        return finalResult;
      }
    });
  }
}
