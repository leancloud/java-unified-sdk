package cn.leancloud;

import cn.leancloud.json.JSON;

import java.util.Map;

class StatusIterator {
  LCStatusQuery.SourceType sourceType;

  // for status query
  LCStatus lastStatus = null;

  // for inbox query
  private int pageSize;
  private long sinceId = LCStatus.INVALID_MESSAGE_ID;
  private long maxId = LCStatus.INVALID_MESSAGE_ID;
  private LCStatusQuery.PaginationDirection direction;

  public StatusIterator(LCStatusQuery.SourceType type) {
    this(type, LCStatusQuery.PaginationDirection.NEW_TO_OLD, 0);
  }

  public StatusIterator(LCStatusQuery.SourceType type, LCStatusQuery.PaginationDirection direction, int pageSize) {
    this.sourceType = type;
    this.direction = direction;
    this.pageSize = pageSize;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    if (pageSize > 0 && pageSize < 200) {
      this.pageSize = pageSize;
    }
  }

  public long getSinceId() {
    return sinceId;
  }

  public void setSinceId(long sinceId) {
    this.sinceId = sinceId;
  }

  public long getMaxId() {
    return maxId;
  }

  public void setMaxId(long maxId) {
    this.maxId = maxId;
  }

  public LCStatusQuery.PaginationDirection getDirection() {
    return direction;
  }

  public void setDirection(LCStatusQuery.PaginationDirection direction) {
    this.direction = direction;
  }

  public void fillConditions(LCQuery query) {
    if (null == query || null == this.lastStatus) {
      return;
    }
    if (LCStatusQuery.PaginationDirection.NEW_TO_OLD == this.direction) {
      query.whereLessThan(LCObject.KEY_CREATED_AT, this.lastStatus.getCreatedAt());
    } else {
      query.whereGreaterThan(LCObject.KEY_CREATED_AT, this.lastStatus.getCreatedAt());
    }
  }

  public void fillConditions(Map<String, String> condition) {
    if (direction == LCStatusQuery.PaginationDirection.OLD_TO_NEW && sinceId > LCStatus.INVALID_MESSAGE_ID) {
      condition.put("sinceId", String.valueOf(sinceId));
    }
    if (direction == LCStatusQuery.PaginationDirection.NEW_TO_OLD && maxId > LCStatus.INVALID_MESSAGE_ID) {
      // need to decrease 1, bcz REST API will query target up to maxId(include).
      condition.put("maxId", String.valueOf(maxId - 1));
    }
  }

  public void encounter(LCStatus status) {
    this.lastStatus = status;
    if (LCStatusQuery.SourceType.INBOX == sourceType && null != status) {
      if (direction == LCStatusQuery.PaginationDirection.OLD_TO_NEW) {
        if (status.getMessageId() > sinceId) {
          sinceId = status.getMessageId();
        }
      } else {
        if (0 == maxId) {
          maxId = status.getMessageId();
        } else if (status.getMessageId() < maxId) {
          maxId = status.getMessageId();
        }
      }
    }
  }

  public String toString() {
    return JSON.toJSONString(this);
  }
}
