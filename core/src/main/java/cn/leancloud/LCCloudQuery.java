package cn.leancloud;

import cn.leancloud.core.PaasClient;
import cn.leancloud.query.LCCloudQueryResult;
import cn.leancloud.query.LCQueryResult;
import cn.leancloud.utils.LCUtils;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import java.util.*;

public class LCCloudQuery {
  private LCCloudQuery() {
  }

  /**
   * execute cql query in background.
   * @param cql cql statement.
   * @return observable instance.
   */
  public static Observable<LCCloudQueryResult> executeInBackground(String cql) {
    return executeInBackground(null, cql);
  }

  /**
   * execute cql query in background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param cql cql statement.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public static Observable<LCCloudQueryResult> executeInBackground(LCUser asAuthenticatedUser, String cql) {
    return executeInBackground(asAuthenticatedUser, cql, LCObject.class);
  }

  /**
   * execute cql query in background.
   * @param cql cql statement.
   * @param params query parameters.
   * @return observable instance.
   */
  public static Observable<LCCloudQueryResult> executeInBackground(String cql, Object... params) {
    return executeInBackground(null, cql, params);
  }

  /**
   * execute cql query in background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param cql cql statement.
   * @param params query parameters.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public static Observable<LCCloudQueryResult> executeInBackground(LCUser asAuthenticatedUser,
                                                                   String cql, Object... params) {
    return executeInBackground(asAuthenticatedUser, cql, LCObject.class, params);
  }

  /**
   * execute cql query in background.
   * @param cql cql statement.
   * @param clazz result class.
   * @return observable instance.
   */
  public static Observable<LCCloudQueryResult> executeInBackground(String cql, Class<? extends LCObject> clazz) {
    return executeInBackground(null, cql, clazz);
  }

  /**
   * execute cql query in background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param cql cql statement.
   * @param clazz result class.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public static Observable<LCCloudQueryResult> executeInBackground(LCUser asAuthenticatedUser,
                                                                   String cql, Class<? extends LCObject> clazz) {
    return executeInBackground(asAuthenticatedUser, cql, clazz, null);
  }

  /**
   * execute cql query in background.
   * @param cql cql statement.
   * @param clazz result class.
   * @param params query parameters.
   * @param <T> template type.
   * @return observable instance.
   */
  public static <T extends LCObject> Observable<LCCloudQueryResult> executeInBackground(String cql, final Class<T> clazz, Object... params) {
    return executeInBackground(null, cql, clazz, params);
  }

  /**
   * execute cql query in background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param cql cql statement.
   * @param clazz result class.
   * @param params query parameters.
   * @param <T> template type.
   * @return observable instance.
   *
   * in general, this method should be invoked in lean engine.
   */
  public static <T extends LCObject> Observable<LCCloudQueryResult> executeInBackground(LCUser asAuthenticatedUser,
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
      p.put("pvalues", LCUtils.jsonStringFromObjectWithNull(pValue));
    }
    return PaasClient.getStorageClient().cloudQuery(asAuthenticatedUser, p).map(new Function<LCQueryResult, LCCloudQueryResult>() {
      public LCCloudQueryResult apply(LCQueryResult LCQueryResult) throws Exception {
        LCCloudQueryResult finalResult = new LCCloudQueryResult();
        List<T> rawObjs = new ArrayList(LCQueryResult.getCount());
        for (LCObject o : LCQueryResult.getResults()) {
          rawObjs.add((T) Transformer.transform(o, LCQueryResult.getClassName()));
        }
        finalResult.setResults(rawObjs);
        finalResult.setCount(LCQueryResult.getCount());

        return finalResult;
      }
    });
  }
}
