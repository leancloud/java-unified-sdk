package cn.leancloud;

import cn.leancloud.annotation.LCClassName;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Transformer {
  private static LCLogger logger = LogUtil.getLogger(Transformer.class);

  private static Pattern classnamePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
  private static final Map<String, Class<? extends LCObject>> subClassesMAP =
          new HashMap<String, Class<? extends LCObject>>();
  private static final Map<Class<? extends LCObject>, String> subClassesReverseMAP =
          new HashMap<Class<? extends LCObject>, String>();

  static Class<? extends LCObject> getSubClass(String className) {
    return subClassesMAP.get(className);
  }

  public static String getSubClassName(Class clazz) {
    if (LCUser.class.isAssignableFrom(clazz)) {
      return LCUser.CLASS_NAME;
    } else if (LCRole.class.isAssignableFrom(clazz)) {
      return LCRole.CLASS_NAME;
    } else if (LCStatus.class.isAssignableFrom(clazz)) {
      return LCStatus.CLASS_NAME;
    } else if (LCFile.class.isAssignableFrom(clazz)) {
      return LCFile.CLASS_NAME;
    } else if (LCFriendship.class.isAssignableFrom(clazz)) {
      return LCFriendship.CLASS_NAME;
    } else if (LCBlockRelation.class.isAssignableFrom(clazz)) {
      return LCBlockRelation.CLASS_NAME;
    } else if (LCFriendshipRequest.class.isAssignableFrom(clazz)) {
      return LCFriendshipRequest.CLASS_NAME;
    } else {
      return subClassesReverseMAP.get(clazz);
    }
  }

  public static <T extends LCObject> void registerClass(Class<T> clazz) {
    LCClassName LCClassName = clazz.getAnnotation(LCClassName.class);
    if (LCClassName == null) {
      throw new IllegalArgumentException("The class is not annotated by @AVClassName");
    }
    String className = LCClassName.value();
    checkClassName(className);
    subClassesMAP.put(className, clazz);
    subClassesReverseMAP.put(clazz, className);
  }

  public static <T extends LCObject> T transform(LCObject rawObj, String className) {
    if (null == rawObj) {
      return null;
    }
    LCObject result = objectFromClassName(className);
    result.resetByRawData(rawObj);
    return (T) result;
  }

  public static <T extends LCObject> T transform(LCObject rawObj, Class<T> clazz) {
    if (null == rawObj) {
      return null;
    }
    LCObject result = null;
    if (subClassesReverseMAP.containsKey(clazz)) {
      try {
        result = clazz.newInstance();
      } catch (Exception ex) {
        logger.w("newInstance failed. cause: " + ex.getMessage());
        result = new LCObject(clazz.getSimpleName());
      }
    } else if (LCUser.class.isAssignableFrom(clazz)) {
      result = new LCUser();
    } else if (LCRole.class.isAssignableFrom(clazz)) {
      result = new LCRole();
    } else if (LCStatus.class.isAssignableFrom(clazz)) {
      result = new LCStatus();
    } else if (LCFile.class.isAssignableFrom(clazz)) {
      result = new LCFile();
    } else if (LCFriendshipRequest.class.isAssignableFrom(clazz)) {
      result = new LCFriendshipRequest();
    } else if (LCFriendship.class.isAssignableFrom(clazz)) {
      result = new LCFriendship();
    } else if (LCBlockRelation.class.isAssignableFrom(clazz)) {
      result = new LCBlockRelation();
    } else {
      result = new LCObject(clazz.getSimpleName());
    }
    result.resetByRawData(rawObj);
    return (T)result;
  }

  public static void checkClassName(String className) {
    if (StringUtil.isEmpty(className))
      throw new IllegalArgumentException("Blank class name");
    if (!classnamePattern.matcher(className).matches())
      throw new IllegalArgumentException("Invalid class name");
  }

  public static LCObject objectFromClassName(String className) {
    LCObject result = null;
    if (LCUser.CLASS_NAME.equals(className)) {
      result = new LCUser();
    } else if (LCStatus.CLASS_NAME.equals(className)) {
      result = new LCStatus();
    } else if (LCRole.CLASS_NAME.equals(className)) {
      result = new LCRole();
    } else if (LCFile.CLASS_NAME.equals(className)) {
      result = new LCFile();
    } else if (LCInstallation.CLASS_NAME.equals(className)) {
      result = new LCInstallation();
    } else if (LCFriendshipRequest.CLASS_NAME.equals(className)) {
      result = new LCFriendshipRequest();
    } else if (LCFriendship.CLASS_NAME.equals(className)) {
      result = new LCFriendship();
    } else if (LCBlockRelation.CLASS_NAME.equals(className)) {
      result = new LCBlockRelation();
    } else if (subClassesMAP.containsKey(className)) {
      try {
        result = subClassesMAP.get(className).newInstance();
      } catch (Exception ex) {
        logger.w("failed to create subClass: " + className, ex);
        result = new LCObject(className);
      }
    } else {
      result = new LCObject(className);
    }
    return result;
  }

  private Transformer() {
  }
}
