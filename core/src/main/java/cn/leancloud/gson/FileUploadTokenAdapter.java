package cn.leancloud.gson;

import cn.leancloud.upload.FileUploadToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
@Deprecated
public class FileUploadTokenAdapter extends TypeAdapter<FileUploadToken> {
  private static final String FIELD_BUCKET = "bucket";
  private static final String FIELD_OBJECTID = "objectId";
  private static final String FIELD_UPLOAD_URL = "upload_url";
  private static final String FIELD_PROVIDER = "provider";
  private static final String FIELD_TOKEN = "token";
  private static final String FIELD_URL = "url";
  private static final String FIELD_KEY = "key";

  public void write(JsonWriter writer, FileUploadToken token) throws IOException {
    writer.beginObject();
    writer.name(FIELD_BUCKET).value(token.getBucket());
    writer.name(FIELD_OBJECTID).value(token.getObjectId());
    writer.name(FIELD_UPLOAD_URL).value(token.getUploadUrl());
    writer.name(FIELD_PROVIDER).value(token.getProvider());
    writer.name(FIELD_TOKEN).value(token.getToken());
    writer.name(FIELD_URL).value(token.getUrl());
    writer.name(FIELD_KEY).value(token.getKey());
    writer.endObject();
    writer.flush();
  }

  public FileUploadToken read(JsonReader reader) throws IOException {
    FileUploadToken fileUploadToken = new FileUploadToken();
    reader.beginObject();
    String fieldName = null;
    JsonToken jsonToken = null;
    while (reader.hasNext()) {
      jsonToken = reader.peek();
      if (jsonToken.equals(JsonToken.NAME)) {
        fieldName = reader.nextName();
      }
      reader.peek();
      String value = reader.nextString();
      if (FIELD_BUCKET.equals(fieldName)) {
        fileUploadToken.setBucket(value);
      }
      if (FIELD_OBJECTID.equals(fieldName)) {
        fileUploadToken.setObjectId(value);
      }
      if (FIELD_UPLOAD_URL.equals(fieldName)) {
        fileUploadToken.setUploadUrl(value);
      }
      if (FIELD_PROVIDER.equals(fieldName)) {
        fileUploadToken.setProvider(value);
      }
      if (FIELD_TOKEN.equals(fieldName)) {
        fileUploadToken.setToken(value);
      }
      if (FIELD_URL.equals(fieldName)) {
        fileUploadToken.setUrl(value);
      }
      if (FIELD_KEY.equals(fieldName)) {
        fileUploadToken.setKey(value);
      }
    }
    reader.endObject();
    return fileUploadToken;
  }
}
