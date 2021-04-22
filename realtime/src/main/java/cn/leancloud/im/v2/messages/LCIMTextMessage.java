package cn.leancloud.im.v2.messages;

import cn.leancloud.im.v2.LCIMTypedMessage;
import cn.leancloud.im.v2.annotation.LCIMMessageField;
import cn.leancloud.im.v2.annotation.LCIMMessageType;

import java.util.Map;

@LCIMMessageType(type = LCIMMessageType.TEXT_MESSAGE_TYPE)
public class LCIMTextMessage extends LCIMTypedMessage {

  public LCIMTextMessage() {

  }

  @LCIMMessageField(name = "_lctext")
  String text;
  @LCIMMessageField(name = "_lcattrs")
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
