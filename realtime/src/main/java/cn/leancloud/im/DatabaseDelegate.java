package cn.leancloud.im;

import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMMessage;

import java.util.List;
import java.util.Map;

public interface DatabaseDelegate {
//  int update(String table, Map<String, Object> attrs, String whereClause);
  int update(String table, Map<String, Object> attrs, String whereClause, String[] whereArgs);
  int delete(String table, String whereClause, String[] whereArgs);
  int insert(String table, Map<String, Object> attrs);
  int queryCount(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having,
            String orderBy);
  List<AVIMMessage> queryMessages(String[] columns, String selection, String[] selectionArgs,
                                  String groupBy, String having, String orderBy, String limit);
  List<AVIMConversation> queryConversations(String[] columns, String selection, String[] selectionArgs,
                                            String groupBy, String having, String orderBy, String limit);
  List<AVIMConversation> rawQueryConversations(String sql, String[] selectionArgs);
}
