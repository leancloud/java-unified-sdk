package cn.leancloud.im.v2;

import junit.framework.TestCase;

public class LCIMMessageOptionTest extends TestCase {
  public LCIMMessageOptionTest(String name) {
    super(name);
  }

  public void testJSONSerializ() {
    LCIMMessageOption option = new LCIMMessageOption();
    option.setPriority(LCIMMessageOption.MessagePriority.High);
    option.setPushData("pushData");
    option.setReceipt(true);
    option.setTransient(false);
    option.setWill(true);

    String jsonString = option.toJSONString();
    System.out.println(jsonString);
    LCIMMessageOption newOption = LCIMMessageOption.parseJSONString(jsonString);
    System.out.println(newOption.toJSONString());
    assertTrue(jsonString.equals(newOption.toJSONString()));
  }
}
