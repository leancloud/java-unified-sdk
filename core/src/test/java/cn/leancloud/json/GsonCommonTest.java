package cn.leancloud.json;

import cn.leancloud.AVObject;
import cn.leancloud.service.AppAccessEndpoint;
import cn.leancloud.sms.AVCaptchaDigest;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GsonCommonTest extends TestCase {
  public GsonCommonTest(String name) {
    super(name);
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
    Object json = ConverterUtils.parseObject(text);
    System.out.println(json.toString());
  }

  public void testSerializeHashMap() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("className", "Student");
    map.put("version", 5);
    map.put("@type", AVObject.class.getName());

    Map<String, Object> data = new HashMap<String, Object>();
    data.put("@type", HashMap.class.getName());
    data.put("age", 5);
    data.put("address", "Beijing City");
    map.put("serverData", data);
    System.out.println(ConverterUtils.toJsonString(map));
  }

  public void testJSONObjectSerialize() {
    JSONObject object = new JSONObject();
    object.put("className", "Student");
    object.put("version", 5);
    String objectString = ConverterUtils.getGsonInstance().toJson(object);
    System.out.println("object jsonString: " + objectString);
    JSONObject other = ConverterUtils.getGsonInstance().fromJson(objectString, JSONObject.class);

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
    responses = ConverterUtils.getGsonInstance().fromJson(draft, ResponseList);
    System.out.println(responses.toString());
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
    AVCaptchaDigest digest = JSON.parseObject(text, AVCaptchaDigest.class);
    System.out.println(JSON.toJSONString(digest));
    assertTrue(digest.getCaptchaUrl().startsWith("https"));
  }
}
