package cn.leancloud.im.v2;

import cn.leancloud.im.v2.messages.LCIMImageMessage;
import cn.leancloud.im.v2.messages.LCIMTextMessage;
import cn.leancloud.utils.StringUtil;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class LCIMMessageTest extends TestCase {
  public LCIMMessageTest(String name) {
    super(name);
  }

  public void testJSONSerializer() throws Exception {
    LCIMMessage originMsg = new LCIMMessage("conversationAlpha", "userBeta", 20000, 30000);
    originMsg.setContent("just a test");
    originMsg.setCurrentClient("clientOmega");
    originMsg.setMentionAll(true);
    originMsg.setMentionListString("@userA, @userB, @userC");
    originMsg.setMessageId("msgId-xfhei-wrw");
    originMsg.setMessageIOType(LCIMMessage.AVIMMessageIOType.AVIMMessageIOTypeIn);
    originMsg.setMessageStatus(LCIMMessage.AVIMMessageStatus.AVIMMessageStatusReceipt);
    originMsg.setUpdateAt(40000);
    originMsg.setReadAt(3232099);
    originMsg.setUniqueToken("uniquetoken-faei-read-8.8");

    String jsonString = originMsg.toJSONString();
    System.out.println(jsonString);
    LCIMMessage newMsg = LCIMMessage.parseJSONString(jsonString);
    System.out.println(newMsg.toJSONString());
    assertTrue(originMsg.equals(newMsg));
  }

  public void testRawDump() throws Exception {
    LCIMMessage originMsg = new LCIMMessage("conversationAlpha", "userBeta", 20000, 30000);
    originMsg.setContent("just a test");
    originMsg.setCurrentClient("clientOmega");
    originMsg.setMentionAll(true);
    originMsg.setMentionListString("@userA, @userB, @userC");
    originMsg.setMessageId("msgId-xfhei-wrw");
    originMsg.setMessageIOType(LCIMMessage.AVIMMessageIOType.AVIMMessageIOTypeIn);
    originMsg.setMessageStatus(LCIMMessage.AVIMMessageStatus.AVIMMessageStatusReceipt);
    originMsg.setUpdateAt(40000);
    originMsg.setReadAt(3232099);
    originMsg.setUniqueToken("uniquetoken-faei-read-8.8");
    originMsg.setTransient(true);

    Map<String, Object> rawDump = originMsg.dumpRawData();
    System.out.println(rawDump);
    assertTrue(rawDump.containsKey("transient"));

    originMsg.setTransient(false);

    rawDump = originMsg.dumpRawData();
    System.out.println(rawDump);
    assertTrue(!rawDump.containsKey("transient"));
  }

  public void testBinaryMessageSerializer() throws Exception {
    LCIMBinaryMessage msg = new LCIMBinaryMessage();
    msg.setBytes(StringUtil.getRandomString(16).getBytes());
    String jsonString = msg.toJSONString();
    System.out.println(jsonString);
    LCIMMessage copyMsg = LCIMMessage.parseJSONString(jsonString);
    assertTrue(copyMsg instanceof LCIMBinaryMessage);
    assertTrue(((LCIMBinaryMessage)copyMsg).getBytes() != null);
  }

  public void testTypedTextMessageSerializer() throws Exception {
    long nowTs = System.currentTimeMillis();
    LCIMTextMessage textMessage = new LCIMTextMessage();
    textMessage.setText("text");
    Map<String, Object> attr = new HashMap<>();
    attr.put("key", nowTs);
    textMessage.setAttrs(attr);
    String jsonString = textMessage.toJSONString();
    System.out.println(jsonString);
    LCIMMessage copyTextMessage = LCIMMessage.parseJSONString(jsonString);
    assertTrue(copyTextMessage instanceof LCIMTextMessage);
  }

  public void testImageMessageSerializer() throws Exception {
    long nowTs = System.currentTimeMillis();
    Map<String, Object> attr = new HashMap<>();
    attr.put("key", nowTs);
    LCIMImageMessage audioMessage = new LCIMImageMessage("../core/20160704174809.jpeg");
    audioMessage.setAttrs(attr);
    audioMessage.setText("listen carefully.");

    String jsonString = audioMessage.toJSONString();
    System.out.println(jsonString);
    LCIMMessage copyAudioMessage = LCIMMessage.parseJSONString(jsonString);
    assertTrue(copyAudioMessage instanceof LCIMImageMessage);
  }

  public void testMessageDeserializer() throws Exception {
    String jsonString = "{\"clientId\":\"23b713e9-1633-4ebf-9243-a3704c279cb8\",\"typeMsgData\":{\"_lctext\":\"d14ee1f0-afd9-438c-8fca-4df10fb70de3\",\"_lctype\":-1}}";
    LCIMMessage message = LCIMMessage.parseJSONString(jsonString);
    assertTrue(message instanceof LCIMTextMessage);
    assertTrue(((LCIMTextMessage) message).getText().equals("d14ee1f0-afd9-438c-8fca-4df10fb70de3"));
  }
}
