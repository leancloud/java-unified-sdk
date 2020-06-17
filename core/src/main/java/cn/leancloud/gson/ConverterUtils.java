package cn.leancloud.gson;

import cn.leancloud.*;
import cn.leancloud.json.JSONObject;
import cn.leancloud.ops.*;
import cn.leancloud.service.AppAccessEndpoint;
import cn.leancloud.sms.AVCaptchaDigest;
import cn.leancloud.sms.AVCaptchaValidateResult;
import cn.leancloud.upload.FileUploadToken;
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
          .registerTypeAdapter(AVObject.class, new ObjectDeserializer())
          .registerTypeAdapter(AVUser.class, new ObjectDeserializer())
          .registerTypeAdapter(AVFile.class, new ObjectDeserializer())
          .registerTypeAdapter(AVRole.class, new ObjectDeserializer())
          .registerTypeAdapter(AVStatus.class, new ObjectDeserializer())
          .registerTypeAdapter(AVInstallation.class, new ObjectDeserializer())
          .registerTypeAdapter(BaseOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(AddOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(AddRelationOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(AddUniqueOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(BitAndOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(BitOrOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(BitXOROperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(CompoundOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(DecrementOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(DeleteOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(IncrementOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(NumericOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(RemoveOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(RemoveRelationOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(SetOperation.class, new BaseOperationAdapter())
          .registerTypeAdapter(GsonObject.class, new JSONObjectAdapter())
          .registerTypeAdapter(JSONObject.class, new JSONObjectAdapter())
          .registerTypeAdapter(GsonArray.class, new JSONArrayAdapter())
          .registerTypeAdapter(FileUploadToken.class, new FileUploadTokenAdapter())
          .registerTypeAdapter(AppAccessEndpoint.class,
                  new GeneralObjectAdapter<>(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES,
                          TypeToken.get(AppAccessEndpoint.class)))
          .registerTypeAdapter(AVCaptchaDigest.class,
                  new GeneralObjectAdapter<>(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES,
                          TypeToken.get(AVCaptchaDigest.class)))
          .registerTypeAdapter(AVCaptchaValidateResult.class,
                  new GeneralObjectAdapter<>(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES,
                          TypeToken.get(AVCaptchaValidateResult.class)))
          .registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(),  new MapDeserializerDoubleAsIntFix())
          .create();

  public static void initialize() {
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
    if (object instanceof GsonObject) {
      return ((GsonObject) object).getRawObject();
    }
    if (object instanceof GsonArray) {
      return ((GsonArray) object).getRawObject();
    }
    return gson.toJsonTree(object);
  }

  public static Object parseObject(String jsonString) {
    return gson.fromJson(jsonString, new TypeToken<Map<String, Object>>(){}.getType());
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
