package cn.leancloud;

public class AVSaveOption {

  AVQuery matchQuery;
  boolean fetchWhenSave;

  /**
   * refresh object value with latest data from remote server after AVObject saved
   *
   * @param fetchWhenSave set true to enable this functionality
   * @return
   */
  public AVSaveOption setFetchWhenSave(boolean fetchWhenSave) {
    this.fetchWhenSave = fetchWhenSave;
    return this;
  }

  /**
   * Only save object when query matches AVObject instance data
   *
   * @param query
   * @return
   */
  public AVSaveOption query(AVQuery query) {
    this.matchQuery = query;
    return this;
  }
}
