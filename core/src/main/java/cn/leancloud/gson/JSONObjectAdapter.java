package cn.leancloud.gson;

import cn.leancloud.json.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class JSONObjectAdapter extends TypeAdapter<JSONObject> {
  private MapDeserializerDoubleAsIntFix mapDeserializerDoubleAsIntFix = new MapDeserializerDoubleAsIntFix();
  public void write(JsonWriter writer, JSONObject object) throws IOException {
    if (!(object instanceof GsonObject)) {
      writer.nullValue();
    } else {
      TypeAdapters.JSON_ELEMENT.write(writer, ((GsonObject) object).getRawObject());
    }
  }

  public JSONObject read(JsonReader reader) throws IOException {
    JsonElement jsonObject = TypeAdapters.JSON_ELEMENT.read(reader);
    return new GsonObject((JsonObject) jsonObject);
  }
}
