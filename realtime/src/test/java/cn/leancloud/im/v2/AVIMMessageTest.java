package cn.leancloud.im.v2;

import junit.framework.TestCase;

public class AVIMMessageTest extends TestCase {
  public AVIMMessageTest(String name) {
    super(name);
  }

  public void testJSONSerializer() throws Exception {
    AVIMMessage originMsg = new AVIMMessage("conversationAlpha", "userBeta", 20000, 30000);
    originMsg.setContent("just a test");
    originMsg.setCurrentClient("clientOmega");
    originMsg.setMentionAll(true);
    originMsg.setMentionListString("@userA, @userB, @userC");
    originMsg.setMessageId("msgId-xfhei-wrw");
    originMsg.setMessageIOType(AVIMMessage.AVIMMessageIOType.AVIMMessageIOTypeIn);
    originMsg.setMessageStatus(AVIMMessage.AVIMMessageStatus.AVIMMessageStatusReceipt);
    originMsg.setUpdateAt(40000);
    originMsg.setReadAt(3232099);
    originMsg.setUniqueToken("uniquetoken-faei-read-8.8");

    String jsonString = originMsg.toJSONString();
    System.out.println(jsonString);
    AVIMMessage newMsg = AVIMMessage.parseJSONString(jsonString);
    System.out.println(newMsg.toJSONString());
    assertTrue(originMsg.equals(newMsg));
  }
}
