package cn.leancloud.gson;

import cn.leancloud.upload.FileUploadToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class FileUploadTokenAdapter extends TypeAdapter<FileUploadToken> {
  private static final String FIELD_BUCKET = "bucket";
  private static final String FIELD_OBJECTID = "objectId";
  private static final String FIELD_UPLOAD_URL = "upload_url";
  private static final String FIELD_PROVIDER = "provider";
  private static final String FIELD_TOKEN = "token";
  private static final String FIELD_URL = "url";
  private static final String FIELD_KEY = "key";

  public void write(JsonWriter writer, FileUploadToken token) throws IOException {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(FIELD_BUCKET, token.getBucket());
    jsonObject.addProperty(FIELD_OBJECTID, token.getObjectId());
    jsonObject.addProperty(FIELD_UPLOAD_URL, token.getUploadUrl());
    jsonObject.addProperty(FIELD_PROVIDER, token.getProvider());
    jsonObject.addProperty(FIELD_TOKEN, token.getToken());
    jsonObject.addProperty(FIELD_URL, token.getUrl());
    jsonObject.addProperty(FIELD_KEY, token.getKey());
    TypeAdapters.JSON_ELEMENT.write(writer, jsonObject);
  }

  public FileUploadToken read(JsonReader reader) throws IOException {
    JsonElement elem = TypeAdapters.JSON_ELEMENT.read(reader);
    if (null != elem && elem.isJsonObject()) {
      JsonObject jsonObject = elem.getAsJsonObject();
      FileUploadToken token = new FileUploadToken();
      if (jsonObject.has(FIELD_BUCKET)) {
        token.setBucket(jsonObject.get(FIELD_BUCKET).getAsString());
      }
      if (jsonObject.has(FIELD_OBJECTID)) {
        token.setObjectId(jsonObject.get(FIELD_OBJECTID).getAsString());
      }
      if (jsonObject.has(FIELD_UPLOAD_URL)) {
        token.setUploadUrl(jsonObject.get(FIELD_UPLOAD_URL).getAsString());
      }
      if (jsonObject.has(FIELD_PROVIDER)) {
        token.setProvider(jsonObject.get(FIELD_PROVIDER).getAsString());
      }
      if (jsonObject.has(FIELD_TOKEN)) {
        token.setToken(jsonObject.get(FIELD_TOKEN).getAsString());
      }
      if (jsonObject.has(FIELD_URL)) {
        token.setUrl(jsonObject.get(FIELD_URL).getAsString());
      }
      if (jsonObject.has(FIELD_KEY)) {
        token.setKey(jsonObject.get(FIELD_KEY).getAsString());
      }
      return token;
    }
    return null;
  }
}
