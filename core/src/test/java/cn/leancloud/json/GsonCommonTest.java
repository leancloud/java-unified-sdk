package cn.leancloud.json;

import cn.leancloud.AVObject;
import junit.framework.TestCase;

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
}
