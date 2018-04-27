package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Transformer {
  private static AVLogger LOGGER = LogUtil.getLogger(Transformer.class);

  private static Pattern CLASSNAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
  private final static Map<String, Class<? extends AVObject>> SUB_CLASSES_MAP =
          new HashMap<String, Class<? extends AVObject>>();
  private final static Map<Class<? extends AVObject>, String> SUB_CLASSES_REVERSE_MAP =
          new HashMap<Class<? extends AVObject>, String>();

  static Class<? extends AVObject> getSubClass(String className) {
    return SUB_CLASSES_MAP.get(className);
  }

  static String getSubClassName(Class<? extends AVObject> clazz) {
    if (AVUser.class.isAssignableFrom(clazz)) {
      return AVUser.CLASS_NAME;
    } else if (AVRole.class.isAssignableFrom(clazz)) {
      return AVRole.CLASS_NAME;
    } else if (AVStatus.class.isAssignableFrom(clazz)) {
      return AVStatus.CLASS_NAME;
    } else if (AVFile.class.isAssignableFrom(clazz)) {
      return AVFile.CLASS_NAME;
    } else {
      return SUB_CLASSES_REVERSE_MAP.get(clazz);
    }
  }

  public static <T extends AVObject> void registerClass(Class<T> clazz) {
    AVClassName avClassName = clazz.getAnnotation(AVClassName.class);
    if (avClassName == null) {
      throw new IllegalArgumentException("The class is not annotated by @AVClassName");
    }
    String className = avClassName.value();
    checkClassName(className);
    SUB_CLASSES_MAP.put(className, clazz);
    SUB_CLASSES_REVERSE_MAP.put(clazz, className);
    // register object serializer/deserializer.
    ParserConfig.getGlobalInstance().putDeserializer(clazz, new ObjectTypeAdapter());
    SerializeConfig.getGlobalInstance().put(clazz, new ObjectTypeAdapter());
  }

  public static <T extends AVObject> T transform(AVObject rawObj, String className) {
    AVObject result = objectFromClassName(className);
    result.resetByRawData(rawObj);
    return (T) result;
  }

  public static <T extends AVObject> T transform(AVObject rawObj, Class<T> clazz) {
    AVObject result = null;
    if (SUB_CLASSES_REVERSE_MAP.containsKey(clazz)) {
      try {
        result = clazz.newInstance();
      } catch (Exception ex) {
        LOGGER.w("newInstance failed. cause: " + ex.getMessage());
        result = new AVObject(clazz.getSimpleName());
      }
    } else if (AVUser.class.isAssignableFrom(clazz)) {
      result = new AVUser();
    } else if (AVRole.class.isAssignableFrom(clazz)) {
      result = new AVRole();
    } else if (AVStatus.class.isAssignableFrom(clazz)) {
      result = new AVStatus();
    } else if (AVFile.class.isAssignableFrom(clazz)) {
      result = new AVFile();
    } else {
      result = new AVObject(clazz.getSimpleName());
    }
    result.resetByRawData(rawObj);
    return (T)result;
  }

  public static void checkClassName(String className) {
    if (StringUtil.isEmpty(className))
      throw new IllegalArgumentException("Blank class name");
    if (!CLASSNAME_PATTERN.matcher(className).matches())
      throw new IllegalArgumentException("Invalid class name");
  }

  public static AVObject objectFromClassName(String className) {
    AVObject result = null;
    if (AVUser.CLASS_NAME.equals(className)) {
      result = new AVUser();
    } else if (AVStatus.CLASS_NAME.equals(className)) {
      result = new AVStatus();
    } else if (AVRole.CLASS_NAME.equals(className)) {
      result = new AVRole();
    } else if (AVFile.CLASS_NAME.equals(className)) {
      result = new AVFile();
    } else if (SUB_CLASSES_MAP.containsKey(className)) {
      System.out.println("create subClass for name: " + className);
      try {
        result = SUB_CLASSES_MAP.get(className).newInstance();
      } catch (Exception ex) {
        System.out.println("failed to create subClass: " + className);
        ex.printStackTrace();
        result = new AVObject(className);
      }
    } else {
      result = new AVObject(className);
    }
    System.out.println("object from class:" + className + " is " + result);
    return result;
  }
}
