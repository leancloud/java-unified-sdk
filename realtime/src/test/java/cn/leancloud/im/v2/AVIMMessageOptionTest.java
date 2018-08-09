package cn.leancloud.im.v2;

import junit.framework.TestCase;

public class AVIMMessageOptionTest extends TestCase {
  public AVIMMessageOptionTest(String name) {
    super(name);
  }

  public void testJSONSerializ() {
    AVIMMessageOption option = new AVIMMessageOption();
    option.setPriority(AVIMMessageOption.MessagePriority.High);
    option.setPushData("pushData");
    option.setReceipt(true);
    option.setTransient(false);
    option.setWill(true);

    String jsonString = option.toJSONString();
    System.out.println(jsonString);
    AVIMMessageOption newOption = AVIMMessageOption.parseJSONString(jsonString);
    System.out.println(newOption.toJSONString());
    assertTrue(jsonString.equals(newOption.toJSONString()));
  }
}
