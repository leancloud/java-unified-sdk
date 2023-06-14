package cn.leancloud.gson;

import cn.leancloud.json.JSONArray;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class JSONArrayAdapter extends TypeAdapter<JSONArray> {
  public void write(JsonWriter writer, JSONArray array) throws IOException {
    if (array instanceof GsonArray) {
      TypeAdapter<JsonElement> elementAdapter = GsonWrapper.getAdapter(JsonElement.class);
      elementAdapter.write(writer,((GsonArray)array).getRawObject());
    } else {
      writer.nullValue();
    }
  }

  public JSONArray read(JsonReader reader) throws IOException {
    TypeAdapter<JsonElement> elementAdapter = GsonWrapper.getAdapter(JsonElement.class);
    JsonElement jsonObject = elementAdapter.read(reader);
    return new GsonArray((JsonArray) jsonObject);
  }
}
