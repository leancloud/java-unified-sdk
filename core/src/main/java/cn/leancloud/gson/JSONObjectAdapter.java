package cn.leancloud.gson;

import cn.leancloud.json.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class JSONObjectAdapter extends TypeAdapter<JSONObject> {
  private MapDeserializerDoubleAsIntFix mapDeserializerDoubleAsIntFix = new MapDeserializerDoubleAsIntFix();
  public void write(JsonWriter writer, JSONObject object) throws IOException {
    if (!(object instanceof GsonObject)) {
      writer.nullValue();
    } else {
      TypeAdapter<JsonElement> elementAdapter = GsonWrapper.getAdapter(JsonElement.class);
      elementAdapter.write(writer, ((GsonObject) object).getRawObject());
    }
  }

  public JSONObject read(JsonReader reader) throws IOException {
    TypeAdapter<JsonElement> elementAdapter = GsonWrapper.getAdapter(JsonElement.class);
    JsonElement jsonObject = elementAdapter.read(reader);
    return new GsonObject((JsonObject) jsonObject);
  }
}
