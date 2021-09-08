package cn.leancloud.im.v2.messages;

import cn.leancloud.LCException;
import cn.leancloud.LCFile;
import cn.leancloud.LCLogger;
import cn.leancloud.callback.ProgressCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.core.PaasClient;
import cn.leancloud.im.v2.LCIMTypedMessage;
import cn.leancloud.im.v2.annotation.LCIMMessageField;
import cn.leancloud.im.v2.annotation.LCIMMessageType;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import io.reactivex.schedulers.Schedulers;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@LCIMMessageType(type = LCIMMessageType.FILE_MESSAGE_TYPE)
public class LCIMFileMessage extends LCIMTypedMessage {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCIMFileMessage.class);

  static final String OBJECT_ID = "objId";
  static final String FILE_URL = "url";
  static final String FILE_META = "metaData";
  static final String FILE_SIZE = "size";
  public static final String FORMAT = "format";
  public static final String DURATION = "duration";
  private static final String LOCAL_PATH = "local_path";

  File localFile;
  LCFile actualFile;
  boolean keepFileName = false;

  boolean hasAdditionalMetaAttr = false;

  @LCIMMessageField(name = "_lcfile")
  protected Map<String, Object> file;
  @LCIMMessageField(name = "_lctext")
  String text;
  @LCIMMessageField(name = "_lcattrs")
  Map<String, Object> attrs;

  ProgressCallback progressCallback;

  public LCIMFileMessage() {
  }

  public LCIMFileMessage(String localPath) throws IOException {
    this(new File(localPath));
  }

  public LCIMFileMessage(File localFile) throws IOException {
    this.localFile = localFile;
    actualFile = new LCFile(localFile.getName(), localFile);
    this.file = new HashMap<String, Object>();
    file.put(LOCAL_PATH, localFile.getPath());
  }

  public LCIMFileMessage(LCFile file) {
    this.actualFile = file;
  }

  public Map<String, Object> getFile() {
    return file;
  }

  /**
   * 获取本地文件地址，如果用户并未指定任何本地文件，则返回 null
   *
   * @return local file path.
   */
  public String getLocalFilePath() {
    return (null != localFile && localFile.exists() ? localFile.getPath() : null);
  }

  /**
   * 获取文件消息中得 LCFile 对象
   *
   * @return LCFile instance.
   */
  public LCFile getLCFile() {
    if (actualFile != null) {
      return actualFile;
    } else if (null != file && file.containsKey(FILE_URL)) {
      Map<String, Object> avfileMeta = null;
      if (file.containsKey(FILE_META)) {
        avfileMeta = (Map) file.get(FILE_META);
      }
      LCFile avfile = new LCFile(null, (String) file.get(FILE_URL), avfileMeta);
      if (file.containsKey(OBJECT_ID)) {
        avfile.setObjectId((String) file.get(OBJECT_ID));
      }
      return avfile;
    }
    return null;
  }

  public void attachLCFile(LCFile file, boolean keepName) {
    this.actualFile = file;
    this.keepFileName = keepName;
  }

  protected void setFile(Map<String, Object> file) {
    this.file = file;
    Map<String, Object> metaData = (Map<String, Object>) file.get(FILE_META);
    actualFile = new LCFile(null, (String) file.get(FILE_URL), metaData);
    actualFile.setObjectId((String) file.get(OBJECT_ID));
    if (file.containsKey(LOCAL_PATH)) {
      this.localFile = new File((String) file.get(LOCAL_PATH));
    }
  }

  protected void setHasAdditionalMetaAttr(boolean hasAdditionalMetaAttr) {
    this.hasAdditionalMetaAttr = hasAdditionalMetaAttr;
  }

  /**
   * 获取文件地址
   *
   * @return file url.
   */
  public String getFileUrl() {
    if (file != null) {
      return (String) file.get(FILE_URL);
    } else {
      return null;
    }
  }

  /**
   * 获取文件的metaData
   *
   * @return meta data map.
   */
  public Map<String, Object> getFileMetaData() {
    if (file == null) {
      file = new HashMap<String, Object>();
    }
    Map<String, Object> meta;
    if (!file.containsKey(FILE_META)) {
      meta = new HashMap<String, Object>();
      meta.put(FILE_SIZE, actualFile.getSize());
      file.put(FILE_META, meta);
    } else {
      meta = (Map<String, Object>) file.get(FILE_META);
    }
    return meta;
  }

  /**
   * 获取文件大小
   *
   * @return file size.
   */
  public long getSize() {
    Map<String, Object> meta = getFileMetaData();
    if (meta != null && meta.containsKey(FILE_SIZE)) {
      return Long.parseLong(meta.get(FILE_SIZE).toString());
    }
    return 0;
  }

  protected void upload(final SaveCallback callback) {
    if (actualFile != null) {
      actualFile.saveInBackground(this.keepFileName).subscribeOn(Schedulers.io())
              .subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(LCException e) {
          if (e != null) {
            callback.internalDone(e);
          } else {
            fulFillFileInfo(callback);
          }
        }
      }));
    } else {
      callback.internalDone(new LCException(new RuntimeException("cannot find the file!")));
    }
  }

  /**
   * 设置文件上传进度回调
   *
   * @param callback callback function.
   */
  public void setProgressCallback(ProgressCallback callback) {
    this.progressCallback = callback;
  }

  public String getText() {
    return this.text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Map<String, Object> getAttrs() {
    return this.attrs;
  }

  public void setAttrs(Map<String, Object> attr) {
    this.attrs = attr;
  }

  protected void fulFillFileInfo(final SaveCallback callback) {
    // fulfill the file info map with LCFile
    if (actualFile != null) {
      file = getFile() == null ? new HashMap<String, Object>() : getFile();
      file.put(OBJECT_ID, actualFile.getObjectId());
      file.put(FILE_URL, actualFile.getUrl());
      file.remove(LOCAL_PATH);
      final Map<String, Object> metaData =
              getFileMetaData() == null ? new HashMap<String, Object>() : getFileMetaData();
      if (!metaData.containsKey(FILE_SIZE)) {
        metaData.put(FILE_SIZE, actualFile.getSize());
      }

      getAdditionalMetaData(metaData, new SaveCallback() {
        @Override
        public void done(LCException e) {
          file.put(FILE_META, metaData);
          if (callback != null) {
            callback.internalDone(e);
          }
        }
      });
    } else {
      callback.internalDone(new LCException(new RuntimeException("cannot find the file!")));
    }
  }

  /**
   * 判断是不是通过外部设置 url 来的 LCFile
   *
   * @param avFile LCFile instance.
   * @return flag indicating the parameter file is external or not.
   */
  static boolean isExternalLCFile(LCFile avFile) {
    return null != avFile
            && null != avFile.getMetaData()
            && avFile.getMetaData().containsKey("__source")
            && avFile.getMetaData().get("__source").equals("external");
  }

  protected String getQueryName() {
    return "";
  }

  protected void parseAdditionalMetaData(final Map<String, Object> meta, JSONObject response) {
    return;
  }

  protected void getAdditionalMetaData(final Map<String, Object> meta, final SaveCallback callback) {
    if (!this.hasAdditionalMetaAttr) {
      callback.internalDone(null);
    } else if (!StringUtil.isEmpty(actualFile.getUrl()) && localFile == null
              && !isExternalLCFile(actualFile)) {
      OkHttpClient client = PaasClient.getGlobalOkHttpClient();
      Request.Builder builder = new Request.Builder();
      final String url = actualFile.getUrl() + getQueryName();
      Call call = client.newCall(builder.url(url).get().build());
      call.enqueue(new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
          LOGGER.d("error encountered while accessing qiniu with url:" + url);
          callback.internalDone(null);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response rawResponse) throws IOException {
          String content = rawResponse.body().string();
          try {
            JSONObject response = JSON.parseObject(content);
            parseAdditionalMetaData(meta, response);

            callback.internalDone(null);
          } catch (Exception ex) {
            callback.internalDone(new LCException(ex));
          }
        }
      });
    } else {
      callback.internalDone(null);
    }
  }

  public int hashCode() {
    return super.hashCode();
  }

  public boolean equals(Object other) {
    return super.equals(other);
  }
}
