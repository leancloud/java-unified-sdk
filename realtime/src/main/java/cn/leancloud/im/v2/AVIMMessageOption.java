package cn.leancloud.im.v2;

import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;

import java.util.Map;

//@JSONType
public class AVIMMessageOption {

  /**
   * 消息等级
   */
  private MessagePriority priority = null;

  /**
   * 是否为暂态消息，默认值为 false
   */
  private boolean isTransient = false;

  /**
   * 是否需要回执，默认为 false
   */
  private boolean isReceipt = false;

  /**
   * 是否设置该消息为下线通知消息
   */
  private boolean isWill = false;

  private String pushData;

  public AVIMMessageOption() {

  }

  public String toJSONString() {
    return JSON.toJSONString(this);
  }

  public static AVIMMessageOption parseJSONString(String content) {
    if (StringUtil.isEmpty(content)) {
      return null;
    }
    return JSON.parseObject(content, AVIMMessageOption.class);
  }

  public void setPriority(MessagePriority priority) {
    this.priority = priority;
  }

  public MessagePriority getPriority() {
    return priority;
  }

  public boolean isTransient() {
    return isTransient;
  }

  public void setTransient(boolean isTransient) {
    this.isTransient = isTransient;
  }

  public boolean isReceipt() {
    return isReceipt;
  }

  public void setReceipt(boolean receipt) {
    isReceipt = receipt;
  }
  public String getPushData() {
    return pushData;
  }

  public void setPushData(String pushData) {
    this.pushData = pushData;
  }

  public void setPushDataEx(Map<String, Object> data) {
    if (null != data) {
      this.pushData = JSON.toJSONString(data);
    }
  }

  public boolean isWill() {
    return isWill;
  }

  /**
   * 设置该消息是否为下线通知消息
   * @param will 若为 true 的话，则为下线通知消息
   */
  public void setWill(boolean will) {
    isWill = will;
  }

  /**
   * 消息优先级的枚举，仅针对聊天室有效
   */
  public enum MessagePriority {
    High(1),
    Normal(2),
    Low(3);

    private int priorityIndex;

    public static MessagePriority getProiority(int index) {
      switch (index) {
        case 1:
          return High;
        case 2:
          return Normal;
        case 3:
          return Low;
        default:
          return null;
      }
    }

    MessagePriority(int priority) {
      this.priorityIndex = priority;
    }

    public int getNumber() {
      return priorityIndex;
    }
  }
}
