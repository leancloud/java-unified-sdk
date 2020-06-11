package cn.leancloud;

import cn.leancloud.json.JSON;

import java.util.Map;

class StatusIterator {
  AVStatusQuery.SourceType sourceType;

  // for status query
  AVStatus lastStatus = null;

  // for inbox query
  private int pageSize;
  private long sinceId = AVStatus.INVALID_MESSAGE_ID;
  private long maxId = AVStatus.INVALID_MESSAGE_ID;
  private AVStatusQuery.PaginationDirection direction;

  public StatusIterator(AVStatusQuery.SourceType type) {
    this(type, AVStatusQuery.PaginationDirection.NEW_TO_OLD, 0);
  }

  public StatusIterator(AVStatusQuery.SourceType type, AVStatusQuery.PaginationDirection direction, int pageSize) {
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

  public AVStatusQuery.PaginationDirection getDirection() {
    return direction;
  }

  public void setDirection(AVStatusQuery.PaginationDirection direction) {
    this.direction = direction;
  }

  public void fillConditions(AVQuery query) {
    if (null == query || null == this.lastStatus) {
      return;
    }
    if (AVStatusQuery.PaginationDirection.NEW_TO_OLD == this.direction) {
      query.whereLessThan(AVObject.KEY_CREATED_AT, this.lastStatus.getCreatedAt());
    } else {
      query.whereGreaterThan(AVObject.KEY_CREATED_AT, this.lastStatus.getCreatedAt());
    }
  }

  public void fillConditions(Map<String, String> condition) {
    if (direction == AVStatusQuery.PaginationDirection.OLD_TO_NEW && sinceId > AVStatus.INVALID_MESSAGE_ID) {
      condition.put("sinceId", String.valueOf(sinceId));
    }
    if (direction == AVStatusQuery.PaginationDirection.NEW_TO_OLD && maxId > AVStatus.INVALID_MESSAGE_ID) {
      // need to decrease 1, bcz REST API will query target up to maxId(include).
      condition.put("maxId", String.valueOf(maxId - 1));
    }
  }

  public void encounter(AVStatus status) {
    this.lastStatus = status;
    if (AVStatusQuery.SourceType.INBOX == sourceType && null != status) {
      if (direction == AVStatusQuery.PaginationDirection.OLD_TO_NEW) {
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
