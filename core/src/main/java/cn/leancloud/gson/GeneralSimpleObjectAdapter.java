package cn.leancloud.gson;

import cn.leancloud.annotation.JsonField;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GeneralSimpleObjectAdapter<T> extends TypeAdapter<T> {
  private Class targetClazz;
  private Map<String, Field> displayFields = new HashMap<>();

  public GeneralSimpleObjectAdapter(Class clazz) {
    this.targetClazz = clazz;
    Field[] fields = clazz.getDeclaredFields();
    for (Field field: fields) {
      String fieldName = field.getName();
      JsonField annotation = field.getAnnotation(JsonField.class);
      if (null != annotation) {
        fieldName = annotation.value();
      }
      displayFields.put(fieldName, field);
    }
  }
  @Override
  public void write(JsonWriter jsonWriter, T t) throws IOException {
    jsonWriter.beginObject();
    Field[] fields = t.getClass().getDeclaredFields();
    for(Field field : fields) {
      field.setAccessible(true);
      String canonicalName = field.getName();
      String outputName = canonicalName;
      JsonField jsonField = field.getAnnotation(JsonField.class);
      if (null != jsonField) {
        outputName = jsonField.value();
      }
      Type valueType = field.getType();
      try {
        if (valueType.equals(Character.class) || valueType.equals(char.class)) {
          char value = field.getChar(t);
          jsonWriter.name(outputName).value(value);
        } else if (valueType.equals(Boolean.class) || valueType.equals(boolean.class)) {
          boolean value = field.getBoolean(t);
          jsonWriter.name(outputName).value(value);
        } else if (valueType.equals(String.class)) {
          String value = (String)field.get(t);
          jsonWriter.name(outputName).value(value);
        } else if (valueType.equals(Integer.class) || valueType.equals(int.class)) {
          Integer value = (Integer) field.get(t);
          jsonWriter.name(outputName).value(value);
        } else if (valueType.equals(Long.class) || valueType.equals(long.class)) {
          Long value = (Long) field.get(t);
          jsonWriter.name(outputName).value(value);
        } else if (valueType.equals(Float.class) || valueType.equals(float.class)) {
          Float value = (Float) field.get(t);
          jsonWriter.name(outputName).value(value);
        } else if (valueType.equals(Double.class) || valueType.equals(double.class)) {
          Double value = (Double) field.get(t);
          jsonWriter.name(outputName).value(value);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    jsonWriter.endObject();
    jsonWriter.flush();
  }

  @Override
  public T read(JsonReader jsonReader) throws IOException {
    try {
      T result = (T)this.targetClazz.newInstance();
      jsonReader.beginObject();
      String jsonFieldName = null;
      while(jsonReader.hasNext()) {
        JsonToken token = jsonReader.peek();
        if (token.equals(JsonToken.NAME)) {
          // get current token.
          jsonFieldName = jsonReader.nextName();
        }
        Field targetField = this.displayFields.get(jsonFieldName);
        // move to next token
        jsonReader.peek();
        try {
          if (null == targetField) {
            jsonReader.skipValue();
            continue;
          }
          targetField.setAccessible(true);
          Object value = null;
          Type valueType = targetField.getType();
          if (valueType.equals(String.class)) {
            value = jsonReader.nextString();
          } else if (valueType.equals(Integer.class) || valueType.equals(int.class)) {
            value = jsonReader.nextInt();
          } else if (valueType.equals(Boolean.class) || valueType.equals(boolean.class)) {
            value = jsonReader.nextBoolean();
          } else if (valueType.equals(Character.class) || valueType.equals(char.class)) {
            value = jsonReader.nextString();
          } else if (valueType.equals(Long.class) || valueType.equals(long.class)) {
            value = jsonReader.nextLong();
          } else if (valueType.equals(Float.class) || valueType.equals(float.class)) {
            value = jsonReader.nextDouble();
          } else if (valueType.equals(Double.class) || valueType.equals(double.class)) {
            value = jsonReader.nextDouble();
          }
          targetField.set(result, value);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      jsonReader.endObject();
      return result;
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
