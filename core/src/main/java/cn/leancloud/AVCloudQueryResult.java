package cn.leancloud;

import java.util.Collections;
import java.util.List;

public class AVCloudQueryResult {
  List<? extends AVObject> results = Collections.emptyList();
  int count;

  public List<? extends AVObject> getResults() {
    return results;
  }

  void setResults(List<? extends AVObject> results) {
    this.results = results;
  }

  public int getCount() {
    return count;
  }

  void setCount(int count) {
    this.count = count;
  }
}
