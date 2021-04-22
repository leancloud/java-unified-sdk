package cn.leancloud.im.v2.messages;

import cn.leancloud.LCFile;
import cn.leancloud.im.v2.annotation.LCIMMessageType;
import cn.leancloud.utils.LCUtils;
import cn.leancloud.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@LCIMMessageType(type = LCIMMessageType.VIDEO_MESSAGE_TYPE)
public class LCIMVideoMessage extends LCIMFileMessage {

  public LCIMVideoMessage() {
    super();
    setHasAdditionalMetaAttr(true);
  }

  public LCIMVideoMessage(String localPath) throws IOException {
    super(localPath);
    setHasAdditionalMetaAttr(true);
  }

  public LCIMVideoMessage(File localFile) throws IOException {
    super(localFile);
    setHasAdditionalMetaAttr(true);
  }

  public LCIMVideoMessage(LCFile file) {
    super(file);
    setHasAdditionalMetaAttr(true);
  }

  /**
   * 获取文件的metaData
   *
   * @return meta data map.
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
      Map<String, Object> meta = LCIMFileMessageAccessor.mediaInfo(localFile);
      meta.put(FILE_SIZE, localFile.length());
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
   * 获取视频的时长
   *
   * @return duration interval.
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
  protected void parseAdditionalMetaData(final Map<String, Object> meta, JSONObject response) {
    if (null == meta || null == response) {
      return;
    }
    JSONObject formatInfo = response.getJSONObject("format");
    if (null != formatInfo) {
      if (formatInfo.containsKey("format_name")) {
        String fileFormat = formatInfo.getString("format_name");
        meta.put(FORMAT, fileFormat);
      }
      if (formatInfo.containsKey("duration")) {
        Double durationInDouble = formatInfo.getDouble("duration");
        meta.put(DURATION, LCUtils.normalize2Double(2, durationInDouble));
      }
      if (formatInfo.containsKey(FILE_SIZE)) {
        long size = formatInfo.getLong(FILE_SIZE);
        meta.put(FILE_SIZE, size);
      }
    }
  }
}
