package cn.leancloud.core.types;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

public class AVDate {
  @JSONField(name = "__type")
  private String type = "Date";

  private String iso = "";

  public AVDate() {
  }

  public AVDate(String dateString) {
    iso = dateString;
  }

  public String getType() {
    return type;
  }

  public void setType(String __type) {
    this.type = __type;
  }

  public String getIso() {
    return iso;
  }

  public void setIso(String iso) {
    this.iso = iso;
  }

  public String jsonString() {
    return JSON.toJSONString(this);
  }
}
