package cn.leancloud;

import java.io.BufferedReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.leancloud.ops.Utils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;

public abstract class EngineHandlerInfo {

  private static final AVLogger LOGGER = LogUtil.getLogger(EngineHandlerInfo.class);

  static final String OBJECT = "object";
  static final String USER = "user";

  public EngineHandlerInfo(String endpoint, Method handlerMethod,
      List<EngineFunctionParamInfo> params, Class<?> returnType) {
    this(endpoint, handlerMethod, params, returnType, null);
  }

  public EngineHandlerInfo(String endpoint, Method handlerMethod,
      List<EngineFunctionParamInfo> params, Class<?> returnType, String hookClass) {
    this.handlerMethod = handlerMethod;
    this.endPoint = endpoint;
    this.methodParameterList = params;
    this.returnType = returnType;
    this.hookClass = hookClass;
  }

  final Method handlerMethod;
  final String endPoint;
  final List<EngineFunctionParamInfo> methodParameterList;
  final Class<?> returnType;
  final String hookClass;

  public Method getHandlerMethod() {
    return handlerMethod;
  }

  public String getEndPoint() {
    return endPoint;
  }

  public List<EngineFunctionParamInfo> getParamList() {
    return methodParameterList;
  }

  public Object execute(HttpServletRequest request, boolean rpcCall) throws Exception {
    StringBuilder sb = new StringBuilder();
    String line = null;
    BufferedReader reader = request.getReader();
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    String requestBody = sb.toString();
    LOGGER.d("request body: " + requestBody);
    Object returnValue = null;
    Object params = this.parseParams(requestBody);
    returnValue =
        methodParameterList.size() == 0 ? handlerMethod.invoke(null)
            : params.getClass().isArray() ? handlerMethod.invoke(null, (Object[]) params)
                : handlerMethod.invoke(null, params);
    returnValue = this.wrapperResponse(returnValue, requestBody, rpcCall);
    return returnValue;
  }

  public abstract Object parseParams(String requestBody) throws InvalidParameterException;

  public Object wrapperResponse(Object returnValue, String requestBody, boolean rpcCall) {
    JSONObject result = new JSONObject();
    result.put("result", Utils.getParsedObject(returnValue, true));
    if (!rpcCall) {
      return JSON.parse(ResponseUtil.filterResponse(result.toJSONString()));
    }
    return result;
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, EngineFunction function) {
    String functionName =
        StringUtil.isEmpty(function.value()) ? method.getName() : function.value();
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    Annotation[][] annotationMatrix = method.getParameterAnnotations();
    Class<?>[] paramTypesArray = method.getParameterTypes();
    for (int index = 0; index < paramTypesArray.length; index++) {
      Annotation[] array = annotationMatrix[index];
      if (array.length == 0) {
        LOGGER.w("Parameters not annotated correctly for EngineFunction:" + functionName);
      } else {
        for (Annotation an : array) {
          if (an instanceof EngineFunctionParam) {
            params.add(new EngineFunctionParamInfo(paramTypesArray[index], ((EngineFunctionParam) an)
                    .value()));
          }
        }
      }
    }
    return new EngineFunctionHandlerInfo(functionName, method, params, method.getReturnType());
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, EngineHook hook) {
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    params.add(new EngineFunctionParamInfo("_User".equals(hook.className()) ? AVUser.class
        : AVObject.class, OBJECT));
    if (EngineHookType.beforeUpdate.equals(hook.type())) {
      return new BeforeUpdateHookHandlerInfo(EndpointParser.getInternalEndpoint(hook.className(),
          hook.type()), method, params, null, hook.className());
    }
    return new EngineHookHandlerInfo(EndpointParser.getInternalEndpoint(hook.className(),
        hook.type()), method, params, null, hook.className());
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, IMHook hook) {
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    params.add(new EngineFunctionParamInfo(Map.class, OBJECT));
    return new IMHookHandlerInfo(hook.type().toString(), method, params, Map.class);
  }
}
