package cn.leancloud;

import cn.leancloud.annotation.LCClassName;

import java.util.List;

@LCClassName("Operation")
public class Operation extends LCObject {
  List<LCFile> photo;//图片

  public List<LCFile> getPhoto() {
    return this.getList("photo");
  }

  public void setPhoto(List<LCFile> photo) {
    this.addAll("photo",photo);
  }
}