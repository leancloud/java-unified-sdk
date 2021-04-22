package cn.leancloud;

import cn.leancloud.core.LeanCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.logging.Log4jAdapter;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

public class LeanEngineTest extends TestCase {
  public LeanEngineTest(String name) {
    super(name);
    AppConfiguration.setLogAdapter(new Log4jAdapter());
    LeanCloud.setRegion(LeanCloud.REGION.NorthChina);
    LeanCloud.setLogLevel(LCLogger.Level.VERBOSE);
    LeanCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public void testRegisterAndParseEngineFunction() throws Exception {
    LeanEngine.register(DummyEngineFunction.class);
    String commonJsonParams = "{\"movie\":\"12 Monkeys\"}";
    String objectParams = "{\"object\":{\"__type\": \"Object\",\n" +
            "  \"className\": \"Post\",\n" +
            "  \"objectId\": \"55a39634e4b0ed48f0c1845c\",\n" +
            "  \"movie\": \"夏洛特烦恼\",\n" +
            "  \"stars\": 5,\n" +
            "  \"comment\": \"夏洛一梦，笑成麻花\"\n" +
            "}}";

    EngineHandlerInfo handlerInfo = LeanEngine.getHandler("averageStars");
    assertTrue(null != handlerInfo);
    Object paramObject = handlerInfo.parseParams(commonJsonParams);
    assertTrue(paramObject != null);

    handlerInfo = LeanEngine.getHandler("dumpObject");
    assertTrue(null != handlerInfo);
    paramObject = handlerInfo.parseParams(objectParams);
    assertTrue(((Object[])paramObject)[0] instanceof LCObject);

    commonJsonParams = "{\"object\":[\"objectId\",\"55a39634e4b0ed48f0c1845c\",\"夏洛一梦，笑成麻花\"]}";
    handlerInfo = LeanEngine.getHandler("testStringList");
    assertTrue(null != handlerInfo);
    paramObject = handlerInfo.parseParams(commonJsonParams);
    assertTrue(((Object[])paramObject)[0] instanceof List);

    commonJsonParams = "{\"object\":[]}";
    handlerInfo = LeanEngine.getHandler("testArrayList");
    assertTrue(null != handlerInfo);
    paramObject = handlerInfo.parseParams(commonJsonParams);
    assertTrue(((Object[])paramObject)[0] instanceof List);

    commonJsonParams = "{\"object\":[{\"movie\":\"12 Monkeys\"}, {\"movie\":\"12 Monkeys\"}, {\"movie\":\"12 Monkeys\"}]}";
    handlerInfo = LeanEngine.getHandler("testArrayList");
    assertTrue(null != handlerInfo);
    paramObject = handlerInfo.parseParams(commonJsonParams);
    assertTrue(((Object[])paramObject)[0] instanceof List);

    commonJsonParams = "{\"param1\":12,\"param2\":123244,\"param3\":12.12,\"param4\":12.1," +
            "\"param5\":1242}";
    handlerInfo = LeanEngine.getHandler("testNumbers");
    assertTrue(null != handlerInfo);
    paramObject = handlerInfo.parseParams(commonJsonParams);
    assertTrue(((Object[])paramObject)[0] instanceof Integer);

    commonJsonParams = "{\"param1\":\"{}\",\"param2\":\"123244\",\"param3\":[],\"param4\":\"true\"," +
            "\"param5\":1242}";
    handlerInfo = LeanEngine.getHandler("testMulti");
    assertTrue(null != handlerInfo);
    paramObject = handlerInfo.parseParams(commonJsonParams);
    assertTrue(((Object[])paramObject)[0] instanceof Map);
  }
}
