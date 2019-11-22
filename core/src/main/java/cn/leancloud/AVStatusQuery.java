package cn.leancloud;

import cn.leancloud.core.PaasClient;
import cn.leancloud.ops.Utils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;

import java.util.List;
import java.util.Map;

public class AVStatusQuery extends AVQuery<AVStatus> {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVStatusQuery.class);

  private int pageSize = 0;
  private long sinceId = 0;
  private long endId = 0;
  private boolean ascending = false;
  private AVUser source = null;
  private AVUser owner = null;
  private String inboxType = null;

  protected AVStatusQuery() {
    super(AVStatus.CLASS_NAME, AVStatus.class);
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
    if (pageSize > 0 && pageSize < 1000) {
      this.pageSize = pageSize;
    }
  }

  public void setAscending(boolean direct) {
    this.ascending = direct;
  }

  void setSource(AVUser source) {
    this.source = source;
  }

  void setOwner(AVUser owner) {
    this.owner = owner;
    if (null != owner) {
      getInclude().add(AVStatus.ATTR_SOURCE);
    }
  }

  void setInboxType(String type) {
    this.inboxType = type;
  }

  @Override
  public Map<String, String> assembleParameters() {
    if (null != this.source) {
      // for status query, need to add inboxType filter into where clause.
      if (!StringUtil.isEmpty(inboxType)) {
        whereEqualTo(AVStatus.ATTR_INBOX_TYPE, inboxType);
      }
    }

    Map<String, String> result = super.assembleParameters();
    if (sinceId > 0) {
      result.put("sinceId", String.valueOf(sinceId));
    }
    if (endId > 0) {
      result.put("endId", String.valueOf(endId));
    }
    if (pageSize > 0) {
      result.put("limit", String.valueOf(pageSize));
    }
    if (null != this.owner) {
      if (!StringUtil.isEmpty(inboxType)) {
        // for inbox query, need to add inboxType filter on the top of parameter, it's different from status query.
        // maybe a bug?
        result.put("inboxType", inboxType);
      }
      String ownerString = new JSONObject(Utils.mapFromAVObject(this.owner, false)).toJSONString();
      result.put("owner", ownerString);
    } else if (null != this.source) {
      String sourceString = new JSONObject(Utils.mapFromAVObject(this.source, false)).toJSONString();
      result.put("source", sourceString);
    }

    return result;
  }

  @Override
  protected Observable<List<AVStatus>> findInBackground(int explicitLimit) {
    if (null == this.owner && null == this.source) {
      return Observable.error(new IllegalArgumentException("source and owner are null, please initialize correctly."));
    }
    if (null != this.owner && !this.owner.isAuthenticated()) {
      return Observable.error(new IllegalStateException("Current User isn't authenticated, please login at first."));
    }

    Map<String, String> query = assembleParameters();
    if (explicitLimit > 0) {
      query.put("limit", "1");
    }

    LOGGER.d(query.toString());
    if (null != this.owner) {
      return PaasClient.getStorageClient().queryInbox(query);
    } else {
      return PaasClient.getStorageClient().queryStatus(query);
    }
  }

  public Observable<List<AVStatus>> nextInBackground() {
    return Observable.error(new UnsupportedOperationException("not support yet."));
  }

  @Override
  public Observable<Integer> countInBackground() {
    if (null == this.owner && null == this.source) {
      return Observable.error(new IllegalArgumentException("source and owner are null, please initialize correctly."));
    }
    if (null != this.owner) {
      return Observable.error(new UnsupportedOperationException("countInBackground doesn't work for inbox query," +
              " please use unreadCountInBackground."));
    }
    Map<String, String> query = assembleParameters();
    query.put("count", "1");
    query.put("limit", "0");
    return PaasClient.getStorageClient().queryCount(AVStatus.CLASS_NAME, query);
  }

  public Observable<JSONObject> unreadCountInBackground() {
    Map<String, String> query = assembleParameters();
    query.put("count", "1");
    query.put("limit", "0");
    return PaasClient.getStorageClient().getInboxCount(query);
  }
}
