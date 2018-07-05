package cn.leancloud.im.v2.messages;

import cn.leancloud.AVException;
import cn.leancloud.AVFile;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.core.PaasClient;
import cn.leancloud.im.v2.annotation.AVIMMessageType;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@AVIMMessageType(type = AVIMMessageType.AUDIO_MESSAGE_TYPE)
public class AVIMAudioMessage extends AVIMFileMessage {

  public AVIMAudioMessage() {
    super();
    setHasAdditionalMetaAttr(true);
  }

  public AVIMAudioMessage(String localPath) throws IOException {
    super(localPath);
    setHasAdditionalMetaAttr(true);
  }

  public AVIMAudioMessage(File localFile) throws IOException {
    super(localFile);
    setHasAdditionalMetaAttr(true);
  }

  public AVIMAudioMessage(AVFile file) {
    super(file);
    setHasAdditionalMetaAttr(true);
  }

  /**
   * 获取文件的metaData
   *
   * @return
   */
  @Override
  public Map<String, Object> getFileMetaData() {
    if (file == null) {
      file = new HashMap<String, Object>();
    }
    if (file.containsKey(FILE_META)) {
      return (Map<String, Object>) file.get(FILE_META);
    }
    if (localFile != null) {
      Map<String, Object> meta = AVIMFileMessageAccessor.mediaInfo(localFile);
      meta.put(FILE_SIZE, actualFile.getSize());
      file.put(FILE_META, meta);
      return meta;
    } else if (actualFile != null) {
      Map<String, Object> meta = actualFile.getMetaData();
      file.put(FILE_META, meta);
      return meta;
    }
    return null;
  }

  /**
   * 获取音频的时长
   *
   * @return
   */
  public double getDuration() {
    Map<String, Object> meta = getFileMetaData();
    if (meta != null && meta.containsKey(DURATION)) {
      return ((Number) meta.get(DURATION)).doubleValue();
    }
    return 0;
  }

  @Override
  protected String getQueryName() {
    return "?avinfo";
  }

  @Override
  protected void parseAdditionalMetaData(final Map<String, Object> meta, JSONObject formatInfo) {
    if (null == meta || null == formatInfo) {
      return;
    }
    String fileFormat = formatInfo.getString("format_name");
    Double durationInDouble = formatInfo.getDouble("duration");
    long size = formatInfo.getLong(FILE_SIZE);
    meta.put(FILE_SIZE, size);
    meta.put(DURATION, durationInDouble);
    meta.put(FORMAT, fileFormat);
  }

  @Override
  protected void getAdditionalMetaData(final Map<String, Object> meta, final SaveCallback callback) {
  }
}
