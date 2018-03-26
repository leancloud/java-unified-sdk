package cn.leancloud.query;

import cn.leancloud.AVObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

public class AVQueryResultTest extends TestCase {
  public AVQueryResultTest(String testname) {
    super(testname);
  }
  public static Test suite() {
    return new TestSuite(AVQueryResultTest.class);
  }

  public static class JSONobjectDemo {

    private String obj;
    private String color;
    private List<Map<String, Object>> parts = new ArrayList<Map<String, Object>>();

    public List<Map<String, Object>> getPart() { return parts; }
    public void setPart(List<Map<String, Object>> parts) { this.parts = parts; }

    public String getObj() { return obj; }
    public void setObj(String obj) { this.obj = obj; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

  }
  public void testPOJOParse() {
    JSONobjectDemo ins = new JSONobjectDemo();
    ins.setColor("red");
    ins.setObj("s");
    String text = JSON.toJSONString(ins);
    System.out.println(text);
    //反序列化
    JSONobjectDemo ins1 = JSON.parseObject(text, JSONobjectDemo.class);
    System.out.println(ins1.getColor());
    System.out.println(ins1.getObj());
  }

  public void testGSONParse() {
    AVQueryResult tmp = new AVQueryResult();
    tmp.setCount( 20 );
    AVObject t = new AVObject("className");
    t.put("objectId", "21wefwearfewr");
    List<AVObject> tt = new ArrayList<AVObject>(1);
    tt.add(t);
    tmp.setResults(tt);
    String jsonOutput = JSON.toJSONString(tmp);
    System.out.println("first output: " + jsonOutput);

    AVQueryResult tmp2 = JSON.parseObject(jsonOutput, AVQueryResult.class);
    System.out.println("dummy count: " + tmp2.getCount());
    System.out.println("dummy result: " + tmp2.toString());

    String content = "{\"count\":63,\"results\":[]}";
    AVQueryResult result = JSON.parseObject(content, AVQueryResult.class);
    System.out.println(result.getCount());
    System.out.println(result.toString());
  }

  public void testAVObjectParse() {
    String content = "{\"createdAt\":\"2018-03-26T03:02:53.984Z\",\"objectId\":\"5ab862dd17d0096887852124\",\"updatedAt\":\"2018-03-26T03:02:53.984Z\"}";
    AVObject result = JSON.parseObject(content, AVObject.class);
    System.out.println(result.getCreatedAt());
    System.out.println(result.toString());
  }
}
