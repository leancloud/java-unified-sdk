package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import com.alibaba.fastjson.annotation.JSONType;

@AVClassName("_Status")
@JSONType(ignores = {"acl", "updatedAt", "uuid"})
public class AVStatus extends AVObject {
  public static String CLASS_NAME = "_Status";
  public AVStatus() {
    super(CLASS_NAME);
  }

  public enum INBOX_TYPE {
    TIMELINE("default"), PRIVATE("private");
    private String type;

    private INBOX_TYPE(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return this.type;
    }
  }

}
