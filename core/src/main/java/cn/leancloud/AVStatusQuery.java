package cn.leancloud;

import cn.leancloud.core.PaasClient;
import cn.leancloud.ops.Utils;
import cn.leancloud.utils.ErrorUtils;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import java.util.List;
import java.util.Map;

public class AVStatusQuery extends AVQuery<AVStatus> {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVStatusQuery.class);

  public enum PaginationDirection {
    NEW_TO_OLD(0),
    OLD_TO_NEW(1);
    PaginationDirection(int v) {
      value = v;
    }
    public int value() {
      return this.value;
    }
    int value;
  }

  private int pageSize = 0;
  private long sinceId = 0;
  private long maxId = 0;
  private PaginationDirection direction = PaginationDirection.NEW_TO_OLD;
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

  public long getMaxId() {
    return maxId;
  }

  public void setMaxId(long endId) {
    this.maxId = endId;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    if (pageSize > 0 && pageSize < 200) {
      this.pageSize = pageSize;
    }
  }

  public void setDirection(PaginationDirection direct) {
    this.direction = direct;
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
    if (null != this.owner) {
      if (direction == PaginationDirection.OLD_TO_NEW && sinceId > 0) {
        result.put("sinceId", String.valueOf(sinceId));
      }
      if (direction == PaginationDirection.NEW_TO_OLD && maxId > 0) {
        result.put("maxId", String.valueOf(maxId));
      }
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
    if (pageSize > 0) {
      result.put("limit", String.valueOf(pageSize));
    }

    return result;
  }

  @Override
  protected Observable<List<AVStatus>> findInBackground(int explicitLimit) {
    if (null == this.owner && null == this.source) {
      return Observable.error(ErrorUtils.illegalArgument("User(source or owner) is null, please initialize correctly."));
    }
    if (null != this.owner && !this.owner.isAuthenticated()) {
      return Observable.error(ErrorUtils.sessionMissingException());
    }

    Map<String, String> query = assembleParameters();
    if (explicitLimit > 0) {
      query.put("limit", "1");
    }

    LOGGER.d(query.toString());
    if (null != this.owner) {
      return PaasClient.getStorageClient().queryInbox(query).doOnNext(new Consumer<List<AVStatus>>() {
        @Override
        public void accept(List<AVStatus> avStatuses) throws Exception {
          if (null == avStatuses || avStatuses.size() < 1) {
            return;
          }
          for (AVStatus status: avStatuses) {
            if (direction == PaginationDirection.OLD_TO_NEW) {
              if (status.getMessageId() > sinceId) {
                sinceId = status.getMessageId();
              }
            } else {
              if (status.getMessageId() < maxId) {
                maxId = status.getMessageId();
              }
            }
          }
          LOGGER.d("next iterator: sinceId=" + sinceId + ", maxId=" + maxId);
        }
      });
    } else {
      return PaasClient.getStorageClient().queryStatus(query);
    }
  }

  @Override
  public Observable<Integer> countInBackground() {
    if (null == this.owner && null == this.source) {
      return Observable.error(ErrorUtils.invalidStateException("User(source or owner) is null, please initialize correctly."));
    }
    if (null != this.owner) {
      return Observable.error(ErrorUtils.invalidStateException("countInBackground doesn't work for inbox query," +
              " please use unreadCountInBackground."));
    }

    Map<String, String> query = assembleParameters();
    query.put("count", "1");
    query.put("limit", "0");
    return PaasClient.getStorageClient().queryCount(AVStatus.CLASS_NAME, query);
  }

  public Observable<JSONObject> unreadCountInBackground() {
    if (null == this.owner || !this.owner.isAuthenticated()) {
      return Observable.error(ErrorUtils.sessionMissingException());
    }
    Map<String, String> query = assembleParameters();
    query.put("count", "1");
    query.put("limit", "0");
    return PaasClient.getStorageClient().getInboxCount(query);
  }
}
