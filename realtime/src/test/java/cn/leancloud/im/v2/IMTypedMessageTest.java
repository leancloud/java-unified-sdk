package cn.leancloud.im.v2;

import cn.leancloud.im.v2.messages.LCIMTextMessage;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class IMTypedMessageTest extends TestCase {
  public IMTypedMessageTest(String name) {
    super(name);
  }

  public void testGetContent4MultiMsg() throws Exception {
    Map<String, Object> attr = new HashMap<>();
    attr.put("time", System.currentTimeMillis());

    LCIMTextMessage textMessage = new LCIMTextMessage();
    textMessage.setText("this is a test");
    textMessage.setAttrs(attr);

    System.out.println(textMessage.getContent());
  }
}
