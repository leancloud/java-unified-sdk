package cn.leancloud;

import cn.leancloud.json.JSONObject;

import java.util.List;
import java.util.Map;

public class DummyEngineFunction {
  @EngineFunction("averageStars")
  public static float getAverageStars(@EngineFunctionParam("movie") String movie)
          throws AVException {
    System.out.println("want to get average stars for movie: " + movie);
    return 3.0f;
  }

  @EngineFunction("dumpObject")
  public static int dumpObject(@EngineFunctionParam("object") AVObject data)
          throws AVException {
    if (null == data) {
      return -1;
    }
    System.out.println(data.toJSONString());
    return 0;
  }

  @EngineFunction("testList")
  public static int testList(@EngineFunctionParam("object") List<JSONObject> data)
          throws AVException {
    if (null == data) {
      return -1;
    }
    System.out.println(data.toString());
    return 0;
  }

  @EngineFunction("testNumbers")
  public static int testNumbers(@EngineFunctionParam("param1") int param1,@EngineFunctionParam("param2") long param2,
                                @EngineFunctionParam("param3") double param3,@EngineFunctionParam("param4") float param4,
                                @EngineFunctionParam("param5") Number param5)
          throws AVException {
    return 0;
  }

  @EngineFunction("testMulti")
  public static int testMulti(@EngineFunctionParam("param1") String param1,
                              @EngineFunctionParam("param2") Map<String, Object> param2,
                              @EngineFunctionParam("param3") List<Object> param3,
                              @EngineFunctionParam("param4") boolean param4,
                              @EngineFunctionParam("param5") Number param5)
          throws AVException {
    return 0;
  }
}
