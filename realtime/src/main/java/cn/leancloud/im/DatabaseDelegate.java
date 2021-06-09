package cn.leancloud.im;

import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMMessageStorage;

import java.util.List;
import java.util.Map;

public interface DatabaseDelegate {
  int update(String table, Map<String, Object> attrs, String whereClause, String[] whereArgs);
  int delete(String table, String whereClause, String[] whereArgs);
  int insert(String table, Map<String, Object> attrs);
  int queryCount(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having,
            String orderBy);
  long countForQuery(String query, String[] selectionArgs);
  LCIMMessageStorage.MessageQueryResult queryMessages(String[] columns, String selection, String[] selectionArgs,
                                                      String groupBy, String having, String orderBy, String limit);
  List<LCIMConversation> queryConversations(String[] columns, String selection, String[] selectionArgs,
                                            String groupBy, String having, String orderBy, String limit);
  List<LCIMConversation> rawQueryConversations(String sql, String[] selectionArgs);
}
