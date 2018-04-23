package cn.leancloud;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class BeforeUpdateHookHandlerInfo extends EngineHookHandlerInfo {

  public BeforeUpdateHookHandlerInfo(String endpoint, Method handlerMethod,
      List<EngineFunctionParamInfo> params, Class returnType, String hookClass) {
    super(endpoint, handlerMethod, params, returnType, hookClass);
  }

  @Override
  public Object wrapperResponse(Object result, String requestBody, boolean rpcCall) {
    return JSON.parseObject(requestBody, Map.class).get("object");
  }

}
