package cn.leancloud;

import cn.leancloud.annotation.AVClassName;

import java.util.List;

@AVClassName("Operation")
public class Operation extends AVObject {
  List<AVFile> photo;//图片

  public List<AVFile> getPhoto() {
    return this.getList("photo");
  }

  public void setPhoto(List<AVFile> photo) {
    this.addAll("photo",photo);
  }
}