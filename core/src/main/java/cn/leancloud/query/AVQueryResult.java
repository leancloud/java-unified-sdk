package cn.leancloud.query;

import cn.leancloud.AVObject;
import cn.leancloud.json.JSON;

import java.util.List;

public class AVQueryResult {
  //@JSONField(name="results")
  private List<AVObject> results = null;

  //@JSONField(name="count")
  private int count = 0;

  //@JSONField(name="className")
  private String className = "";

  public List<AVObject> getResults() {
    return results;
  }

  public void setResults(List<AVObject> results) {
    this.results = results;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String toString() {
    return "{\"count\":"+ this.count + ", \"results\":" + this.results + "}";
  }

  public String toJSONString() {
    return JSON.toJSONString(this);
  }

  public static AVQueryResult fromJSONString(String content) {
    return JSON.parseObject(content, AVQueryResult.class);
  }
}
