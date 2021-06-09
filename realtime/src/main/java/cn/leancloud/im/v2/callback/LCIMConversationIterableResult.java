package cn.leancloud.im.v2.callback;

import cn.leancloud.utils.StringUtil;

import java.util.List;

public class LCIMConversationIterableResult {
  private List<String> members = null;
  private String next = null;

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }

  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public boolean hasNext() {
    return !StringUtil.isEmpty(next);
  }
}
