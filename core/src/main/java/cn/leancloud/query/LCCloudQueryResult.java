package cn.leancloud.query;

import cn.leancloud.LCObject;

import java.util.Collections;
import java.util.List;

/**
 *
 * CloudQuery的返回值
 *
 * 包含符合CloudQuery的一组对象 和 符合query的对象数（在指定了count查询的时候 比如："select count(*) from _User"）
 *
 */
public class LCCloudQueryResult {
  List<? extends LCObject> results = Collections.emptyList();
  int count;

  public List<? extends LCObject> getResults() {
    return results;
  }

  public void setResults(List<? extends LCObject> results) {
    this.results = results;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}

