package cn.leancloud;

public class LCSaveOption {

  LCQuery matchQuery;
  boolean fetchWhenSave;

  /**
   * refresh object value with latest data from remote server after AVObject saved
   *
   * @param fetchWhenSave set true to enable this functionality
   * @return this object.
   */
  public LCSaveOption setFetchWhenSave(boolean fetchWhenSave) {
    this.fetchWhenSave = fetchWhenSave;
    return this;
  }

  /**
   * Only save object when query matches AVObject instance data
   *
   * @param query query instance.
   * @return this object.
   */
  public LCSaveOption query(LCQuery query) {
    this.matchQuery = query;
    return this;
  }
}
