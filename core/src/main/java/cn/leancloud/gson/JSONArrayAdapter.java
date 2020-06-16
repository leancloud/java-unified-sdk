package cn.leancloud.gson;

import cn.leancloud.json.JSONArray;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class JSONArrayAdapter extends TypeAdapter<JSONArray> {
  public void write(JsonWriter writer, JSONArray object) throws IOException {
    TypeAdapters.JSON_ELEMENT.write(writer, object.getRawObject());
  }

  public JSONArray read(JsonReader reader) throws IOException {
    JsonElement jsonObject = TypeAdapters.JSON_ELEMENT.read(reader);
    return new JSONArray((JsonArray) jsonObject);
  }
}
