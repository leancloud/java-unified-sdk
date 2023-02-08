package cn.leancloud.gson;

import cn.leancloud.utils.StringUtil;
import com.google.gson.*;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class GeneralFieldMappingObjectAdapter<T> extends TypeAdapter<T> {
  private Class targetClazz;
  private Map<String, Type> displayFields;
  private Map<String, Type> canonicalFields;
  private FieldNamingPolicy fieldNamingPolicy;

  public GeneralFieldMappingObjectAdapter(Class clazz, Map<String, Type> fields, FieldNamingPolicy policy) {
    this.targetClazz = clazz;
    this.displayFields = fields;
    this.fieldNamingPolicy = policy;
    this.canonicalFields = new HashMap<>();
    if (null != fields) {
      for(Map.Entry<String, Type> entry : fields.entrySet()) {
        String displayName = entry.getKey();
        String identifyName = toCanonicalName(displayName, policy);
        this.canonicalFields.put(identifyName, entry.getValue());
      }
    }
  }

  @Override
  public void write(JsonWriter jsonWriter, T t) throws IOException {
    JsonObject jsonObject = new JsonObject();
    Field[] fields = t.getClass().getDeclaredFields();
    for(Field field : fields) {
      field.setAccessible(true);
      String canonicalName = field.getName();
      String outputName = toDisplayName(canonicalName, this.fieldNamingPolicy);
      Type valueType = this.canonicalFields.get(canonicalName);
      try {
        if (valueType.equals(Character.class)) {
          char value = field.getChar(t);
          jsonObject.addProperty(outputName, value);
        } else if (valueType.equals(Boolean.class)) {
          boolean value = field.getBoolean(t);
          jsonObject.addProperty(outputName, value);
        } else if (valueType.equals(String.class)) {
          String value = (String)field.get(t);
          jsonObject.addProperty(outputName, value);
        } else if (valueType.equals(Integer.class)) {
          Integer value = (Integer) field.get(t);
          jsonObject.addProperty(outputName, value);
        } else if (valueType.equals(Long.class)) {
          Long value = (Long) field.get(t);
          jsonObject.addProperty(outputName, value);
        } else if (valueType.equals(Float.class)) {
          Float value = (Float) field.get(t);
          jsonObject.addProperty(outputName, value);
        } else if (valueType.equals(Double.class)) {
          Double value = (Double) field.get(t);
          jsonObject.addProperty(outputName, value);
        }
      } catch (Exception ex) {
        ;
      }
    }
    TypeAdapters.JSON_ELEMENT.write(jsonWriter, jsonObject);
  }

  @Override
  public T read(JsonReader jsonReader) throws IOException {
    JsonElement elem = TypeAdapters.JSON_ELEMENT.read(jsonReader);
    JsonObject jsonObject = elem.getAsJsonObject();
    try {
      T result = (T)this.targetClazz.newInstance();
      for (Map.Entry<String, Type> entry: this.displayFields.entrySet()) {
        String fieldName = entry.getKey();
        Type valueType = entry.getValue();
        JsonElement value = jsonObject.get(fieldName);
        String identifyFieldName = toCanonicalName(fieldName, this.fieldNamingPolicy);
        try {
          Field field = result.getClass().getDeclaredField(identifyFieldName);
          field.setAccessible(true);
          if (valueType.equals(String.class)) {
            field.set(result, value.getAsString());
          } else if (valueType.equals(Integer.class)) {
            field.set(result, value.getAsInt());
          } else if (valueType.equals(Boolean.class)) {
            field.set(result, value.getAsBoolean());
          } else if (valueType.equals(Character.class)) {
            field.set(result, value.getAsByte());
          } else if (valueType.equals(Long.class)) {
            field.set(result, value.getAsLong());
          } else if (valueType.equals(Float.class)) {
            field.set(result, value.getAsFloat());
          } else if (valueType.equals(Double.class)) {
            field.set(result, value.getAsDouble());
          } else {
            field.set(result, value);
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      return result;
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
  private static String toCanonicalName(String name, FieldNamingPolicy fromPolicy) {

    if (FieldNamingPolicy.IDENTITY == fromPolicy) {
      return name;
    }
    String splitter = "";
    if (FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES == fromPolicy) {
      splitter = "_";
    } else if (FieldNamingPolicy.LOWER_CASE_WITH_DASHES == fromPolicy) {
      splitter = "-";
    } else if (FieldNamingPolicy.LOWER_CASE_WITH_DOTS == fromPolicy) {
      splitter = ".";
    }
    if (StringUtil.isEmpty(name) || StringUtil.isEmpty(splitter)) {
      return name;
    }

    StringBuilder sb = new StringBuilder();
    String camels[] = name.split(splitter);
    for (String camel: camels) {
      if (StringUtil.isEmpty(camel)) {
        continue;
      }
      if (sb.length() == 0) {
        sb.append(camel);
      } else {
        sb.append(camel.substring(0, 1).toUpperCase());
        sb.append(camel.substring(1).toLowerCase());
      }
    }
    return sb.toString();
  }

  private static String toDisplayName(String name, FieldNamingPolicy targetPolicy) {
    if (FieldNamingPolicy.IDENTITY == targetPolicy) {
      return name;
    }
    String splitter = "";
    if (FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES == targetPolicy) {
      splitter = "_";
    } else if (FieldNamingPolicy.LOWER_CASE_WITH_DASHES == targetPolicy) {
      splitter = "-";
    } else if (FieldNamingPolicy.LOWER_CASE_WITH_DOTS == targetPolicy) {
      splitter = ".";
    }
    if (StringUtil.isEmpty(name) || StringUtil.isEmpty(splitter)) {
      return name;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      String s = name.substring(i, i+1);
      if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
        sb.append(splitter);
      }
      sb.append(s.toLowerCase());
    }

    return sb.toString();
  }
}
