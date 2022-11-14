package cn.leancloud.json;

import cn.leancloud.LCObject;
import cn.leancloud.gson.GsonObject;
import cn.leancloud.gson.GsonWrapper;
import cn.leancloud.gson.MapDeserializerDoubleAsIntFix;
import cn.leancloud.gson.NumberDeserializerDoubleAsIntFix;
import cn.leancloud.service.AppAccessEndpoint;
import cn.leancloud.sms.LCCaptchaDigest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GsonCommonTest extends TestCase {
  public GsonCommonTest(String name) {
    super(name);
  }

  public void testIllegalData() {
    String content = "{\"_lctype\":103,\"_lcfile\":{\"metaData\":{\"duration\":2.56,\"size\":7244,\"format\":null},\"objId\":\"613707e0f1f73f70cfa19048\",\"url\":\"https:\\/\\/f.letsniyan.com\\/20flALVeg8GiJntS1n1RHYCDT7BptaFt\\/record_1630996438180\"},\"_lctext\":null,\"_lcattrs\":{\"toUserDestroyed\":false,\"fromUserDestroyed\":true}}";
    Gson gson = new GsonBuilder().serializeNulls()
            .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
            .registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(),  new MapDeserializerDoubleAsIntFix())
            .registerTypeAdapter(Map.class,  new MapDeserializerDoubleAsIntFix())
            .setLenient().create();
    Map<String, Object> object = gson.fromJson(content, Map.class);
    System.out.println(object);
    Object lcType = object.get("_lctype");
    System.out.println(lcType);
  }
  private void parseData(String s) {
    Object parsedObject = null;
    try {
      JSONObject tmpObject = JSON.parseObject(s);
      if (null != tmpObject && tmpObject.containsKey("result")) {
        parsedObject = tmpObject.get("result");
      } else {
        parsedObject = tmpObject;
      }
    } catch (Exception exception) {
      // compatible for existing cache data.
      parsedObject = JSON.parse(s);
    }
    if (parsedObject instanceof Number) {
      parsedObject = NumberDeserializerDoubleAsIntFix.parsePrecisionNumber((Number) parsedObject);
    }
    assertTrue(null != parsedObject);
    System.out.println(parsedObject);
  }
  public void testSinglePrimitives() {
    String s = "432423423485";
    parseData(s);
    s = "String from hello";
    parseData(s);
    s = "{'test': true}";
    parseData(s);
    s = "{'result': true}";
    parseData(s);
    s = "[43243, 433, 2]";
    parseData(s);
  }

  public void testPrimitives() {
    System.out.println(GsonWrapper.parseObject("test", String.class));
    System.out.println(GsonWrapper.parseObject("100", Integer.class));
    System.out.println(GsonWrapper.parseObject("100", Short.class));
    System.out.println(GsonWrapper.parseObject("19.9", Double.class));
    System.out.println(GsonWrapper.parseObject("1993472843", Long.class));
    System.out.println(GsonWrapper.parseObject("false", Boolean.class));
    System.out.println(GsonWrapper.parseObject("true", Boolean.class));
//    System.out.println(GsonWrapper.parseObject('t', Byte.class));
//    System.out.println(GsonWrapper.parseObject('r', Character.class));
    System.out.println(GsonWrapper.parseObject("12987.83245", Float.class));

    String text = "{\"devices\":[\n" +
            "\t{\n" +
            "\t\t\"CURRENT_TEMPERATURE\":\"255.255\",\n" +
            "\t\t\"STAT_MODE\":\n" +
            "\t\t\t{\n" +
            "\t\t\t\"MANUAL_OFF\":true,\n" +
            "\t\t\t\"TIMECLOCK\":true\n" +
            "\t\t\t},\n" +
            "\t\t\"TIMER\":false,\n" +
            "\t\t\"device\":\"Watering System\"\n" +
            "\t}\n" +
            "]}";
    Object json = GsonWrapper.parseObject(text, Map.class);
    System.out.println(json.toString());
  }

  public void testDeserializeNestedJson() {
    String text = "{\"devices\":[\n" +
            "\t{\n" +
            "\t\t\"CURRENT_TEMPERATURE\":\"255.255\",\n" +
            "\t\t\"STAT_MODE\":\n" +
            "\t\t\t{\n" +
            "\t\t\t\"MANUAL_OFF\":true,\n" +
            "\t\t\t\"TIMECLOCK\":true\n" +
            "\t\t\t},\n" +
            "\t\t\"TIMER\":false,\n" +
            "\t\t\"device\":\"Watering System\"\n" +
            "\t}\n" +
            "]}";
    Object json = GsonWrapper.parseObject(text);
    System.out.println(json.toString());
  }

  public void testSerializeHashMap() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("className", "Student");
    map.put("version", 5);
    map.put("@type", LCObject.class.getName());

    Map<String, Object> data = new HashMap<String, Object>();
    data.put("@type", HashMap.class.getName());
    data.put("age", 5);
    data.put("address", "Beijing City");
    map.put("serverData", data);
    System.out.println(GsonWrapper.toJsonString(map));
  }

  public void testJSONObjectSerialize() {
    JSONObject object = JSONObject.Builder.create(null);
    object.put("className", "Student");
    object.put("version", 5);
    String objectString = GsonWrapper.getGsonInstance().toJson(object);
    System.out.println("object jsonString: " + objectString);
    JSONObject other = GsonWrapper.getGsonInstance().fromJson(objectString, JSONObject.class);

    assertEquals(object.getInteger("version"), other.getInteger("version"));
    assertEquals(object.getString("className"), other.getString("className"));
  }

  public void testDoubleAndLong() {
    String draft = "[ {\"id\":4077395,\"field_id\":242566,\"body\":\"\"},\n" +
            "  {\"id\":4077398,\"field_id\":242569,\"body\":[[273019,0],[273020,1],[273021,0]]},\n" +
            "  {\"id\":4077399,\"field_id\":242570,\"body\":[[273022,0],[273023,1],[273024,0]]}\n" +
            "]";
    ArrayList<Map<String, Object>> responses;
    Type ResponseList = new TypeToken<ArrayList<Map<String, Object>>>() {}.getType();
    responses = GsonWrapper.getGsonInstance().fromJson(draft, ResponseList);
    System.out.println(responses.toString());
  }

  public void testNumberParser() {
    Number numbers[] = {3, 4.5, 5.0, -0, 0.0, 0.0002, -0.0002, -5, -6.5};
    for (Number num: numbers) {
      System.out.println("original: " + num + ", floor: " + Math.floor(num.doubleValue()) + ", ceil: " + Math.ceil(num.doubleValue())
              + ", parsed: " + NumberDeserializerDoubleAsIntFix.parsePrecisionNumber(num));
    }
  }

  public void testAppAccessEndpoint() {
    String text = "{\"ttl\":3600,\"stats_server\":\"https://stats_server\", \"push_server\": \"https://push_server\"," +
            " \"rtm_router_server\": \"https://rtm_router_server\", \"api_server\": \"https://api_server\"," +
            " \"engine_server\": \"https://engine_server\"}";
    AppAccessEndpoint endpoint = JSON.parseObject(text, AppAccessEndpoint.class);
    System.out.println(JSON.toJSONString(endpoint));
    assertTrue(endpoint.getTtl() == 3600);
    assertTrue(endpoint.getApiServer().endsWith("api_server"));
    assertTrue(endpoint.getEngineServer().endsWith("engine_server"));
    assertTrue(endpoint.getPushServer().endsWith("push_server"));
    assertTrue(endpoint.getRtmRouterServer().endsWith("rtm_router_server"));
    assertTrue(endpoint.getStatsServer().endsWith("stats_server"));
  }

  public void testAVCaptchaDigest() {
    String text = "{\"captcha_token\":\"fhaeifhepfewifh\", \"captcha_url\": \"https://captcha_url\"}";
    LCCaptchaDigest digest = JSON.parseObject(text, LCCaptchaDigest.class);
    System.out.println(JSON.toJSONString(digest));
    assertTrue(digest.getCaptchaUrl().startsWith("https"));
  }
}
