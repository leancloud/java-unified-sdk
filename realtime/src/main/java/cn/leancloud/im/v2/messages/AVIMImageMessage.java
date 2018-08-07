package cn.leancloud.im.v2.messages;

import cn.leancloud.AVFile;
import cn.leancloud.im.v2.annotation.AVIMMessageType;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@AVIMMessageType(type = AVIMMessageType.IMAGE_MESSAGE_TYPE)
public class AVIMImageMessage extends AVIMFileMessage {
  public static final String IMAGE_HEIGHT = "height";
  public static final String IMAGE_WIDTH = "width";

  public AVIMImageMessage() {
    super();
    setHasAdditionalMetaAttr(true);
  }

  public AVIMImageMessage(String localPath) throws IOException {
    super(localPath);
    setHasAdditionalMetaAttr(true);
  }

  public AVIMImageMessage(File localFile) throws IOException {
    super(localFile);
    setHasAdditionalMetaAttr(true);
  }

  public AVIMImageMessage(AVFile file) {
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
      Map<String, Object> meta = AVIMFileMessageAccessor.getImageMeta(localFile);
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
   * 获取图片的高
   *
   * @return
   */
  public int getHeight() {
    Map<String, Object> metaData = getFileMetaData();
    if (metaData != null && metaData.containsKey(IMAGE_HEIGHT)) {
      return parseIntValue(metaData.get(IMAGE_HEIGHT));
    }
    return 0;
  }

  /**
   * 获取图片的宽度
   *
   * @return
   */
  public int getWidth() {
    Map<String, Object> metaData = getFileMetaData();
    if (metaData != null && metaData.containsKey(IMAGE_WIDTH)) {
      return parseIntValue(metaData.get(IMAGE_WIDTH));
    }
    return 0;
  }

  private static int parseIntValue(Object value) {
    if (null != value) {
      if (value instanceof Integer || value instanceof Long) {
        return (int) value;
      } else if (value instanceof Double) {
        return (int) ((double) value);
      } else if (value instanceof BigDecimal) {
        return ((BigDecimal) value).intValue();
      }
    }
    return 0;
  }

  @Override
  protected String getQueryName() {
    return "?imageInfo";
  }
  @Override
  protected void parseAdditionalMetaData(final Map<String, Object> meta, JSONObject response) {
    if (null == meta || null == response) {
      return;
    }
    meta.put(FORMAT, response.getString(FORMAT));
    meta.put(IMAGE_HEIGHT, response.getInteger(IMAGE_HEIGHT));
    meta.put(IMAGE_WIDTH, response.getInteger(IMAGE_WIDTH));
  }
}