package cn.leancloud.fastjson;

import cn.leancloud.json.ConverterFactory;
import cn.leancloud.json.JSONParser;

public class FastJsonConverterFactory implements ConverterFactory {
  public retrofit2.Converter.Factory generateRetrofitConverterFactory() {
    return retrofit2.converter.fastjson.FastJsonConverterFactory.create();
  }

  public JSONParser createJSONParser() {
    return new FastJsonParser();
  }
}
