package cn.leancloud.query;

import cn.leancloud.AVObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class AVQueryResult {
  @JSONField(name="results")
  private List<AVObject> results = null;

  @JSONField(name="count")
  private int count = 0;

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

  public String toString() {
    return "{\"count\":"+ this.count + ", \"results\":" + this.results + "}";
  }
}
