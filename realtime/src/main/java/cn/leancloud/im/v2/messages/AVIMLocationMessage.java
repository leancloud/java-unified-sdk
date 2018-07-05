package cn.leancloud.im.v2.messages;

import cn.leancloud.im.v2.AVIMTypedMessage;
import cn.leancloud.im.v2.annotation.AVIMMessageField;
import cn.leancloud.im.v2.annotation.AVIMMessageType;
import cn.leancloud.types.AVGeoPoint;

import java.util.Map;

@AVIMMessageType(type = AVIMMessageType.LOCATION_MESSAGE_TYPE)
public class AVIMLocationMessage extends AVIMTypedMessage {
  public AVIMLocationMessage() {

  }


  @AVIMMessageField(name = "_lcloc")
  AVGeoPoint location;
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

  public AVGeoPoint getLocation() {
    return location;
  }

  public void setLocation(AVGeoPoint location) {
    this.location = location;
  }
}