package cn.leancloud;

import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public class AVStatusQuery extends AVQuery<AVStatus> {
  private int pageSize = 100;
  private long sinceId = 0;
  private long endId = 0;
  private String inboxType;
  private AVUser owner;

  public AVStatusQuery() {
    super(AVStatus.CLASS_NAME, AVStatus.class);
    getInclude().add(AVStatus.ATTR_SOURCE);
  }

  public void setSinceId(long sinceId) {
    this.sinceId = sinceId;
  }

  public long getSinceId() {
    return sinceId;
  }

  public long getEndId() {
    return endId;
  }

  public void setEndId(long endId) {
    this.endId = endId;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }


  protected String getInboxType() {
    return this.inboxType;
  }

  void setInboxType(String inboxType) {
    this.inboxType = inboxType;
  }

  AVUser getOwner() {
    return owner;
  }

  void setOwner(AVUser owner) {
    this.owner = owner;
  }

  @Override
  public Map<String, String> assembleParameters() {
    if (inboxType != null) {
      this.whereEqualTo("inboxType", inboxType);
    }
    super.assembleParameters();
    Map<String, String> p = this.getParameters();
    if (owner != null) {
      String ownerId = owner.getObjectId();
      Map<String, Object> ownerMap = new HashMap<>();
      ownerMap.put("__type", "Pointer");
      ownerMap.put("className", "_User");
      ownerMap.put("objectId", ownerId);
      p.put("owner", JSON.toJSONString(ownerMap));
    }
    if (sinceId > 0) {
      p.put("sinceId", String.valueOf(sinceId));
    }
    if (!StringUtil.isEmpty(inboxType)) {
      p.put("inboxType", inboxType);
    }
    if (endId > 0) {
      p.put("endId", String.valueOf(endId));
    }
    conditions.setParameters(p);
    return p;
  }
}
