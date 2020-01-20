package cn.leancloud.im.v2;

import cn.leancloud.im.v2.messages.AVIMAudioMessage;
import cn.leancloud.im.v2.messages.AVIMImageMessage;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import cn.leancloud.utils.StringUtil;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

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

  public void testBinaryMessageSerializer() throws Exception {
    AVIMBinaryMessage msg = new AVIMBinaryMessage();
    msg.setBytes(StringUtil.getRandomString(16).getBytes());
    String jsonString = msg.toJSONString();
    System.out.println(jsonString);
    AVIMMessage copyMsg = AVIMMessage.parseJSONString(jsonString);
    assertTrue(copyMsg instanceof AVIMBinaryMessage);
    assertTrue(((AVIMBinaryMessage)copyMsg).getBytes() != null);
  }

  public void testTypedTextMessageSerializer() throws Exception {
    long nowTs = System.currentTimeMillis();
    AVIMTextMessage textMessage = new AVIMTextMessage();
    textMessage.setText("text");
    Map<String, Object> attr = new HashMap<>();
    attr.put("key", nowTs);
    textMessage.setAttrs(attr);
    String jsonString = textMessage.toJSONString();
    System.out.println(jsonString);
    AVIMMessage copyTextMessage = AVIMMessage.parseJSONString(jsonString);
    assertTrue(copyTextMessage instanceof AVIMTextMessage);
  }

  public void testImageMessageSerializer() throws Exception {
    long nowTs = System.currentTimeMillis();
    Map<String, Object> attr = new HashMap<>();
    attr.put("key", nowTs);
    AVIMImageMessage audioMessage = new AVIMImageMessage("../core/20160704174809.jpeg");
    audioMessage.setAttrs(attr);
    audioMessage.setText("listen carefully.");

    String jsonString = audioMessage.toJSONString();
    System.out.println(jsonString);
    AVIMMessage copyAudioMessage = AVIMMessage.parseJSONString(jsonString);
    assertTrue(copyAudioMessage instanceof AVIMImageMessage);
  }
}
