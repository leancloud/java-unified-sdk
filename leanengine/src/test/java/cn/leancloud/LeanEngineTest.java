package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.logging.Log4jAdapter;
import junit.framework.TestCase;

import java.util.Map;

public class LeanEngineTest extends TestCase {
  public LeanEngineTest(String name) {
    super(name);
    AppConfiguration.setLogAdapter(new Log4jAdapter());
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
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

    EngineHandlerInfo commonJsonParamsHandler = LeanEngine.getHandler("averageStars");
    assertTrue(null != commonJsonParamsHandler);
    Object paramObject = commonJsonParamsHandler.parseParams(commonJsonParams);
    assertTrue(paramObject != null);

    EngineHandlerInfo objectParamsHandler = LeanEngine.getHandler("dumpObject");
    assertTrue(null != objectParamsHandler);
    paramObject = objectParamsHandler.parseParams(objectParams);
    assertTrue(((Object[])paramObject)[0] instanceof AVObject);
  }
}
