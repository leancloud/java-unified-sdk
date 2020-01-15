package cn.leancloud.im.v2;

import cn.leancloud.im.v2.messages.AVIMTextMessage;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class AVIMTypedMessageTest extends TestCase {
  public AVIMTypedMessageTest(String name) {
    super(name);
  }

  public void testGetContent4MultiMsg() throws Exception {
    Map<String, Object> attr = new HashMap<>();
    attr.put("time", System.currentTimeMillis());

    AVIMTextMessage textMessage = new AVIMTextMessage();
    textMessage.setText("this is a test");
    textMessage.setAttrs(attr);

    System.out.println(textMessage.getContent());
  }
}
