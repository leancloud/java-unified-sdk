package cn.leancloud;

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
}
