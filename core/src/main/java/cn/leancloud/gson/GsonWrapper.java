package cn.leancloud.gson;

import cn.leancloud.*;
import cn.leancloud.json.JSONObject;
import cn.leancloud.ops.*;
import cn.leancloud.service.AppAccessEndpoint;
import cn.leancloud.sms.LCCaptchaDigest;
import cn.leancloud.sms.LCCaptchaValidateResult;
import cn.leancloud.upload.FileUploadToken;
import com.google.gson.*;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GsonWrapper {
  static final ObjectDeserializer objectDeserializer = new ObjectDeserializer();
  static final BaseOperationAdapter baseOperationAdapter = new BaseOperationAdapter();
  static final JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapter();
  static final JSONArrayAdapter jsonArrayAdapter = new JSONArrayAdapter();
  static final Gson gson = new GsonBuilder().serializeNulls()
          .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
          .registerTypeAdapter(LCObject.class, objectDeserializer)
          .registerTypeAdapter(LCUser.class, objectDeserializer)
          .registerTypeAdapter(LCFile.class, objectDeserializer)
          .registerTypeAdapter(LCRole.class, objectDeserializer)
          .registerTypeAdapter(LCStatus.class, objectDeserializer)
          .registerTypeAdapter(LCInstallation.class, objectDeserializer)
          .registerTypeAdapter(LCFriendshipRequest.class, objectDeserializer)
          .registerTypeAdapter(LCFriendship.class, objectDeserializer)
          .registerTypeAdapter(BaseOperation.class, baseOperationAdapter)
          .registerTypeAdapter(AddOperation.class, baseOperationAdapter)
          .registerTypeAdapter(AddRelationOperation.class, baseOperationAdapter)
          .registerTypeAdapter(AddUniqueOperation.class, baseOperationAdapter)
          .registerTypeAdapter(BitAndOperation.class, baseOperationAdapter)
          .registerTypeAdapter(BitOrOperation.class, baseOperationAdapter)
          .registerTypeAdapter(BitXOROperation.class, baseOperationAdapter)
          .registerTypeAdapter(CompoundOperation.class, baseOperationAdapter)
          .registerTypeAdapter(DecrementOperation.class, baseOperationAdapter)
          .registerTypeAdapter(DeleteOperation.class, baseOperationAdapter)
          .registerTypeAdapter(IncrementOperation.class, baseOperationAdapter)
          .registerTypeAdapter(NumericOperation.class, baseOperationAdapter)
          .registerTypeAdapter(RemoveOperation.class, baseOperationAdapter)
          .registerTypeAdapter(RemoveRelationOperation.class, baseOperationAdapter)
          .registerTypeAdapter(SetOperation.class, baseOperationAdapter)
          .registerTypeAdapter(GsonObject.class, jsonObjectAdapter)
          .registerTypeAdapter(JSONObject.class, jsonObjectAdapter)
          .registerTypeAdapter(GsonArray.class, jsonArrayAdapter)
          .registerTypeAdapter(FileUploadToken.class, new FileUploadTokenAdapter())
          .registerTypeAdapter(AppAccessEndpoint.class,
                  new GeneralObjectAdapter<>(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES,
                          TypeToken.get(AppAccessEndpoint.class)))
          .registerTypeAdapter(LCCaptchaDigest.class,
                  new GeneralObjectAdapter<>(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES,
                          TypeToken.get(LCCaptchaDigest.class)))
          .registerTypeAdapter(LCCaptchaValidateResult.class,
                  new GeneralObjectAdapter<>(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES,
                          TypeToken.get(LCCaptchaValidateResult.class)))
          .registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(),  new MapDeserializerDoubleAsIntFix())
          .registerTypeAdapter(Map.class,  new MapDeserializerDoubleAsIntFix())
          .setLenient()
          .create();

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
    if (object instanceof GsonObject) {
      return ((GsonObject) object).getRawObject();
    }
    if (object instanceof GsonArray) {
      return ((GsonArray) object).getRawObject();
    }
    return gson.toJsonTree(object);
  }


  public static Object parseObject(String jsonString) {
    try {
      return gson.fromJson(jsonString, new TypeToken<Map<String, Object>>() {}.getType());
    } catch (Exception ex) {
      // string is not json/map.
      JsonElement element = gson.toJsonTree(jsonString);
      if (element.isJsonPrimitive()) {
        JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
        if (jsonPrimitive.isBoolean()) {
          return jsonPrimitive.getAsBoolean();
        } else if (jsonPrimitive.isString()) {
          return jsonPrimitive.getAsString();
        } else if (jsonPrimitive.isNumber()) {
          return NumberDeserializerDoubleAsIntFix.parsePrecisionNumber(jsonPrimitive.getAsNumber());
        } else {
          return null;
        }
      } else if (element.isJsonArray()) {
        return element.getAsJsonArray();
      } else {
        return null;
      }
    }
  }

  public static <T> T parseObject(String jsonString, Class<T> clazz) {
    if (clazz.isPrimitive() || String.class.isAssignableFrom(clazz)) {
      JsonElement element = gson.toJsonTree(jsonString);
      return gson.fromJson(element, clazz);
    } else {
      return gson.fromJson(jsonString, clazz);
    }
  }

  public static <T> T parseObject(String jsonString, Type typeOfT) {
    if (Primitives.isPrimitive(typeOfT)
            || (typeOfT instanceof Class && String.class.isAssignableFrom((Class)typeOfT))) {
      JsonElement element = gson.toJsonTree(jsonString, typeOfT);
      return gson.fromJson(element, typeOfT);
    } else {
      return gson.fromJson(jsonString, typeOfT);
    }
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
