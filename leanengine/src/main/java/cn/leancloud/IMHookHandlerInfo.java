package cn.leancloud;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

class IMHookHandlerInfo extends EngineHandlerInfo {

  public IMHookHandlerInfo(String endpoint, Method handlerMethod,
      List<EngineFunctionParamInfo> params, Class returnType) {
    super(endpoint, handlerMethod, params, returnType);
  }

  @Override
  public Object parseParams(String requestBody) {
    return JSON.parseObject(requestBody, Map.class);
  }
}
