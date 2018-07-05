package cn.leancloud.im.v2.messages;

import cn.leancloud.im.v2.AVIMTypedMessage;
import cn.leancloud.im.v2.annotation.AVIMMessageField;
import cn.leancloud.im.v2.annotation.AVIMMessageType;

import java.util.Map;

@AVIMMessageType(type = AVIMMessageType.TEXT_MESSAGE_TYPE)
public class AVIMTextMessage extends AVIMTypedMessage {

  public AVIMTextMessage() {

  }

  @AVIMMessageField(name = "_lctext")
  String text;
  @AVIMMessageField(name = "_lcattrs")
  Map<String, Object> attrs;

  public String getText() {
    return this.text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Map<String, Object> getAttrs() {
    return this.attrs;
  }

  public void setAttrs(Map<String, Object> attr) {
    this.attrs = attr;
  }
}
