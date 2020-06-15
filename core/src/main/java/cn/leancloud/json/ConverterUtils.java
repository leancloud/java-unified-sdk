package cn.leancloud.json;

import cn.leancloud.*;
import cn.leancloud.utils.StringUtil;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConverterUtils {
  static Gson gson = new GsonBuilder().serializeNulls()
          .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
//          .registerTypeAdapterFactory(new TypeAdapterFactory() {
//            @Override
//            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
//              Class<? super T> clazz = typeToken.getRawType();
//              if (clazz == AVObject.class || clazz == AVUser.class || clazz == AVFile.class
//                      || clazz == AVRole.class || clazz == AVStatus.class || clazz == AVInstallation.class) {
//                return new ObjectTypeAdapter();
//              } else if (!StringUtil.isEmpty(Transformer.getSubClassName(clazz))) {
//                return new ObjectTypeAdapter();
//              }
//              return null;
//            }
//          })
          .create();

  public static void initialize() {

//    ParserConfig.getGlobalInstance().putDeserializer(AVObject.class, adapter);
//    ParserConfig.getGlobalInstance().putDeserializer(AVUser.class, adapter);
//    ParserConfig.getGlobalInstance().putDeserializer(AVFile.class, adapter);
//    ParserConfig.getGlobalInstance().putDeserializer(AVRole.class, adapter);
//    ParserConfig.getGlobalInstance().putDeserializer(AVStatus.class, adapter);
//    ParserConfig.getGlobalInstance().putDeserializer(AVInstallation.class, adapter);
//
//    SerializeConfig.getGlobalInstance().put(AVObject.class, adapter);
//    SerializeConfig.getGlobalInstance().put(AVUser.class, adapter);
//    SerializeConfig.getGlobalInstance().put(AVFile.class, adapter);
//    SerializeConfig.getGlobalInstance().put(AVRole.class, adapter);
//    SerializeConfig.getGlobalInstance().put(AVStatus.class, adapter);
//    SerializeConfig.getGlobalInstance().put(AVInstallation.class, adapter);

//    BaseOperationAdapter opAdapter = new BaseOperationAdapter();
//    ParserConfig.getGlobalInstance().putDeserializer(BaseOperation.class, opAdapter);
//    SerializeConfig.getGlobalInstance().put(BaseOperation.class, opAdapter);
  }

  public static Gson getGsonInstance() {
    return gson;
  }

  public static String toJsonString(Map<String, Object> map) {
    if (null == map) {
      return null;
    }
    return gson.toJson(map);
  }

  public static JsonElement toJsonElement(Object object) {
    if (null == object) {
      return null;
    }
    return gson.toJsonTree(object);
  }

  public static Object parseObject(String jsonString) {
    return gson.fromJson(jsonString, Object.class);
  }

  public static <T> T parseObject(String jsonString, Class<T> clazz) {
    return gson.fromJson(jsonString, clazz);
  }

  public static <T> T parseObject(String jsonString, Type typeOfT) {
    return gson.fromJson(jsonString, typeOfT);
  }

  public static <T> T toJavaObject(JsonElement element, Class<T> clazz) {
    if (null == element) {
      return null;
    }
    return gson.fromJson(element, clazz);
  }

  public static Object toJavaObject(JsonElement element) {
    if (null == element) {
      return null;
    }
    return toJavaObject(element, Object.class);
  }

  public static TimeZone defaultTimeZone  = TimeZone.getDefault();
  public static Locale defaultLocale    = Locale.getDefault();

  public static String DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public static final Date castToDate(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof Calendar) {
      return ((Calendar) value).getTime();
    }

    if (value instanceof Date) {
      return (Date) value;
    }

    long longValue = -1;

    if (value instanceof BigDecimal) {
      BigDecimal decimal = (BigDecimal) value;
      int scale = decimal.scale();
      if (scale >= -100 && scale <= 100) {
        longValue = decimal.longValue();
      } else {
        longValue = decimal.longValueExact();
      }
    } else if (value instanceof Number) {
      longValue = ((Number) value).longValue();
    } else if (value instanceof String) {
      String strVal = (String) value;

      if (strVal.indexOf('-') != -1) {
        String format;
        if (strVal.length() == DEFFAULT_DATE_FORMAT.length()) {
          format = DEFFAULT_DATE_FORMAT;
        } else if (strVal.length() == 10) {
          format = "yyyy-MM-dd";
        } else if (strVal.length() == "yyyy-MM-dd HH:mm:ss".length()) {
          format = "yyyy-MM-dd HH:mm:ss";
        } else if (strVal.length() == 29
                && strVal.charAt(26) == ':'
                && strVal.charAt(28) == '0') {
          format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        } else {
          format = "yyyy-MM-dd HH:mm:ss.SSS";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format, defaultLocale);
        dateFormat.setTimeZone(defaultTimeZone);
        try {
          return (Date) dateFormat.parse(strVal);
        } catch (ParseException e) {
          throw new IllegalArgumentException("can not cast to Date, value : " + strVal);
        }
      }

      if (strVal.length() == 0 //
              || "null".equals(strVal)) {
        return null;
      }

      longValue = Long.parseLong(strVal);
    }

    if (longValue < 0) {
      throw new IllegalArgumentException("can not cast to Date, value : " + value);
    }

    return new Date(longValue);
  }
}
