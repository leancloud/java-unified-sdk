package cn.leancloud.im.v2.callback;

import java.util.List;

public class AVIMOperationFailure {
  String reason = "";
  int code = 0;
  List<String> memberIds = null;

  /**
   * 默认构造函数
   */
  public AVIMOperationFailure() {
    ;
  }

  /**
   * 返回错误原因描述
   * @return
   */
  public String getReason() {
    return reason;
  }

  /**
   * 设置错误原因描述
   * @param reason
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * 返回错误码
   * @return
   */
  public int getCode() {
    return code;
  }

  /**
   * 设置错误码
   * @param code
   */
  public void setCode(int code) {
    this.code = code;
  }

  /**
   * 获取出错的 member id 列表
   * @return
   */
  public List<String> getMemberIds() {
    return memberIds;
  }

  /**
   * 设置出错的 member id 列表
   * @param memberIds
   */
  public void setMemberIds(List<String> memberIds) {
    this.memberIds = memberIds;
  }

  /**
   * 返回出错的 member id 列表长度
   * @return
   */
  public int getMemberIdCount() {
    return (null == this.memberIds)? 0 : this.memberIds.size();
  }

}
