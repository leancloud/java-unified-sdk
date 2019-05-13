package cn.leancloud.push.lite;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AVObjectDeserializer implements ObjectDeserializer {
  static final String LOG_TAG = AVObjectDeserializer.class.getSimpleName();
  public static final AVObjectDeserializer instance = new AVObjectDeserializer();

  @Override
  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
    if (AVInstallation.class.isAssignableFrom((Class) type)) {
      Map<String, Object> objectMap = new HashMap<String, Object>();
      parser.parseObject(objectMap);
      AVInstallation object = null;
      try {
        object = (AVInstallation) ((Class) type).newInstance();
        object.setClassName((String) objectMap.get("className"));
        object.setObjectId((String) objectMap.get("objectId"));
        object.setCreatedAt((String) objectMap.get("createdAt"));
        object.setUpdatedAt((String) objectMap.get("updatedAt"));
        if (objectMap.containsKey("serverData")) {
          object.serverData.putAll((Map<String, Object>) objectMap.get("serverData"));
        }
      } catch (InstantiationException e) {

      } catch (IllegalAccessException e) {

      } catch (Exception e) {

      } finally {
        return (T) object;
      }
    }
    return (T) parser.parseObject();
  }
}
