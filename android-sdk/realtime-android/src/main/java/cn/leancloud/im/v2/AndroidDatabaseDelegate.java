package cn.leancloud.im.v2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.leancloud.AVLogger;
import cn.leancloud.codec.Base64Decoder;
import cn.leancloud.im.DatabaseDelegate;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_ATTRIBUTE;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_BREAKPOINT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONVERSATION_ID;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONVERSATION_READAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONVRESATION_DELIVEREDAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_LASTMESSAGE_INNERTYPE;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_MENTIONED;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_SYSTEM;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_TEMP;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_TEMP_TTL;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CREATEDAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CREATOR;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_DEDUPLICATED_TOKEN;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_FROM_PEER_ID;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_INSTANCEDATA;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_LASTMESSAGE;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_LM;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MEMBERS;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MESSAGE_DELIVEREDAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MESSAGE_READAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MESSAGE_UPDATEAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MSG_INNERTYPE;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MSG_MENTION_ALL;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MSG_MENTION_LIST;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_PAYLOAD;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_STATUS;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_TIMESTAMP;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_TRANSIENT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_UNREAD_COUNT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_UPDATEDAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.CONVERSATION_TABLE;
import static cn.leancloud.im.v2.AVIMMessageStorage.DB_NAME_PREFIX;
import static cn.leancloud.im.v2.AVIMMessageStorage.INTEGER;
import static cn.leancloud.im.v2.AVIMMessageStorage.MESSAGE_INNERTYPE_BIN;
import static cn.leancloud.im.v2.AVIMMessageStorage.MESSAGE_TABLE;
import static cn.leancloud.im.v2.AVIMMessageStorage.NUMBERIC;
import static cn.leancloud.im.v2.AVIMMessageStorage.TEXT;

/**
 * Created by fengjunwen on 2018/8/9.
 */

public class AndroidDatabaseDelegate implements DatabaseDelegate {
  private static final AVLogger LOGGER = LogUtil.getLogger(AndroidDatabaseDelegate.class);

  static class DBHelper extends SQLiteOpenHelper {
    static final String MESSAGE_CREATE_SQL =
        "CREATE TABLE IF NOT EXISTS " + MESSAGE_TABLE + " ("
            + COLUMN_CONVERSATION_ID + " VARCHAR(32) NOT NULL, "
            + AVIMMessageStorage.COLUMN_MESSAGE_ID + " VARCHAR(32) NOT NULL, "
            + COLUMN_TIMESTAMP + " NUMBERIC, "
            + COLUMN_FROM_PEER_ID + " TEXT NOT NULL, "
            + COLUMN_MESSAGE_DELIVEREDAT + " NUMBERIC, "
            + COLUMN_MESSAGE_READAT + " NUMBERIC, "
            + COLUMN_MESSAGE_UPDATEAT + " NUMBERIC, "
            + COLUMN_PAYLOAD + " BLOB, "
            + COLUMN_STATUS + " INTEGER, "
            + COLUMN_BREAKPOINT + " INTEGER, "
            + COLUMN_DEDUPLICATED_TOKEN + " VARCHAR(32), "
            + COLUMN_MSG_MENTION_ALL + " INTEGER default 0, "
            + COLUMN_MSG_MENTION_LIST + " TEXT NULL, "
            + COLUMN_MSG_INNERTYPE + " INTEGER default 0, "
            + "PRIMARY KEY(" + COLUMN_CONVERSATION_ID + ","
            + AVIMMessageStorage.COLUMN_MESSAGE_ID + ")) ";

    static final String MESSAGE_UNIQUE_INDEX_SQL =
        "CREATE UNIQUE INDEX IF NOT EXISTS " + AVIMMessageStorage.MESSAGE_INDEX + " on "
            + MESSAGE_TABLE + " (" + COLUMN_CONVERSATION_ID
            + ", " + COLUMN_TIMESTAMP + ", " + AVIMMessageStorage.COLUMN_MESSAGE_ID + ") ";

    static final String CONVERSATION_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
        + CONVERSATION_TABLE + " ("
        + COLUMN_CONVERSATION_ID + " VARCHAR(32) NOT NULL,"
        + AVIMMessageStorage.COLUMN_EXPIREAT + " NUMBERIC,"
        + COLUMN_ATTRIBUTE + " BLOB,"
        + COLUMN_INSTANCEDATA + " BLOB,"
        + COLUMN_UPDATEDAT + " VARCHAR(32),"
        + COLUMN_CREATEDAT + " VARCHAR(32),"
        + COLUMN_CREATOR + " TEXT,"
        + COLUMN_MEMBERS + " TEXT,"
        + COLUMN_TRANSIENT + " INTEGER,"
        + COLUMN_UNREAD_COUNT + " INTEGER,"
        + COLUMN_CONVERSATION_READAT + " NUMBERIC,"
        + COLUMN_CONVRESATION_DELIVEREDAT + " NUMBERIC,"
        + COLUMN_LM + " NUMBERIC,"
        + COLUMN_LASTMESSAGE + " TEXT,"
        + COLUMN_CONV_MENTIONED + " INTEGER default 0,"
        + COLUMN_CONV_LASTMESSAGE_INNERTYPE + " INTEGER default 0, "
        + COLUMN_CONV_SYSTEM + " INTEGER default 0, "
        + COLUMN_CONV_TEMP + " INTEGER default 0, "
        + COLUMN_CONV_TEMP_TTL + " NUMBERIC, "
        + "PRIMARY KEY(" + COLUMN_CONVERSATION_ID + "))";

    public DBHelper(Context context, String clientId) {
      super(context, getDatabasePath(clientId), null, AVIMMessageStorage.DB_VERSION);
    }

    private static String getDatabasePath(String clientId) {
      // 要 MD5 ?
      return DB_NAME_PREFIX + clientId;
    }

    private static String getAddColumnSql(String table, String column, String type) {
      return String.format("ALTER TABLE %s ADD COLUMN %s %s;", table, column, type);
    }

    private static String getAddColumnSql(String table, String column, String type, String defaultV) {
      return String.format("ALTER TABLE %s ADD COLUMN %s %s default %s;", table, column, type, defaultV);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
      sqLiteDatabase.execSQL(MESSAGE_CREATE_SQL);
      sqLiteDatabase.execSQL(MESSAGE_UNIQUE_INDEX_SQL);
      sqLiteDatabase.execSQL(CONVERSATION_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
      if (oldVersion == 1) {
        upgradeToVersion2(sqLiteDatabase);
        oldVersion += 1;
      }
      if (oldVersion == 2) {
        upgradeToVersion3(sqLiteDatabase);
        oldVersion += 1;
      }
      if (oldVersion == 3) {
        upgradeToVersion4(sqLiteDatabase);
        oldVersion += 1;
      }
      if (oldVersion == 4) {
        upgradeToVersion5(sqLiteDatabase);
        oldVersion += 1;
      }
      if (oldVersion == 5) {
        upgradeToVersion6(sqLiteDatabase);
        oldVersion += 1;
      }
      if (oldVersion == 6) {
        upgradeToVersion7(sqLiteDatabase);
        oldVersion += 1;
      }
      if (oldVersion == 7) {
        upgradeToVersion8(sqLiteDatabase);
        oldVersion += 1;
      }
      if (oldVersion == 8) {
        upgradeToVersion9(sqLiteDatabase);
        oldVersion += 1;
      }
      if (oldVersion == 9) {
        upgradeToVersion10(sqLiteDatabase);
        oldVersion += 1;
      }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
      db.execSQL(CONVERSATION_CREATE_SQL);
    }

    private void upgradeToVersion3(SQLiteDatabase db) {
      try {
        if (!columnExists(db, MESSAGE_TABLE, COLUMN_DEDUPLICATED_TOKEN)) {
          db.execSQL(getAddColumnSql(MESSAGE_TABLE,
              COLUMN_DEDUPLICATED_TOKEN, AVIMMessageStorage.VARCHAR32));
        }
      } catch (Exception ex) {
        LOGGER.w("failed to execute upgrade instrument. cause: " + ex.getMessage());
      }
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
      try {
        if (!columnExists(db, CONVERSATION_TABLE, COLUMN_LASTMESSAGE)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE,
              COLUMN_LASTMESSAGE, TEXT));
        }
      } catch (Exception ex) {
        LOGGER.w("failed to execute upgrade instrument. cause: " + ex.getMessage());
      }
    }

    private void upgradeToVersion5(SQLiteDatabase db) {
      try {
        if (!columnExists(db, CONVERSATION_TABLE, COLUMN_INSTANCEDATA)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, COLUMN_INSTANCEDATA,
              AVIMMessageStorage.BLOB));
        }
      } catch (Exception ex) {
        LOGGER.w("failed to execute upgrade instrument. cause: " + ex.getMessage());
      }
    }

    private void upgradeToVersion6(SQLiteDatabase db) {
      try {
        if (!columnExists(db, CONVERSATION_TABLE, COLUMN_UNREAD_COUNT)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, COLUMN_UNREAD_COUNT, INTEGER));
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, COLUMN_CONVERSATION_READAT, NUMBERIC));
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, COLUMN_CONVRESATION_DELIVEREDAT, NUMBERIC));
        }
        if (!columnExists(db, MESSAGE_TABLE, COLUMN_MESSAGE_READAT)) {
          db.execSQL(getAddColumnSql(MESSAGE_TABLE, COLUMN_MESSAGE_READAT, NUMBERIC));
        }
      } catch (Exception ex) {
        LOGGER.w("failed to execute upgrade instrument. cause: " + ex.getMessage());
      }
    }

    private void upgradeToVersion7(SQLiteDatabase db) {
      try {
        if (!columnExists(db, MESSAGE_TABLE, COLUMN_MESSAGE_UPDATEAT)) {
          db.execSQL(getAddColumnSql(MESSAGE_TABLE, COLUMN_MESSAGE_UPDATEAT, NUMBERIC));
        }
      } catch (Exception ex) {
        LOGGER.w("failed to execute upgrade instrument. cause: " + ex.getMessage());
      }
    }

    private void upgradeToVersion8(SQLiteDatabase db) {
      try {
        if (!columnExists(db, MESSAGE_TABLE, COLUMN_MSG_MENTION_ALL)) {
          db.execSQL(getAddColumnSql(MESSAGE_TABLE, COLUMN_MSG_MENTION_ALL, INTEGER, "0"));
        }
        if (!columnExists(db, MESSAGE_TABLE, COLUMN_MSG_MENTION_LIST)) {
          db.execSQL(getAddColumnSql(MESSAGE_TABLE, COLUMN_MSG_MENTION_LIST, TEXT));
        }
        if (!columnExists(db, CONVERSATION_TABLE, COLUMN_CONV_MENTIONED)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, COLUMN_CONV_MENTIONED, INTEGER, "0"));
        }
      } catch (Exception ex) {
        LOGGER.w("failed to execute upgrade instrument. cause: " + ex.getMessage());
      }
    }

    private void upgradeToVersion9(SQLiteDatabase db) {
      try {
        if (!columnExists(db, MESSAGE_TABLE, COLUMN_MSG_INNERTYPE)) {
          db.execSQL(getAddColumnSql(MESSAGE_TABLE, COLUMN_MSG_INNERTYPE, INTEGER, "0"));
        }
        if (!columnExists(db, CONVERSATION_TABLE, COLUMN_CONV_LASTMESSAGE_INNERTYPE)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, COLUMN_CONV_LASTMESSAGE_INNERTYPE, INTEGER, "0"));
        }
      } catch (Exception ex) {
        LOGGER.w("failed to execute upgrade instrument. cause: " + ex.getMessage());
      }
    }

    private void upgradeToVersion10(SQLiteDatabase db) {
      try {
        if (!columnExists(db, CONVERSATION_TABLE, COLUMN_CONV_SYSTEM)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, COLUMN_CONV_SYSTEM, INTEGER, "0"));
        }
        if (!columnExists(db, CONVERSATION_TABLE, COLUMN_CONV_TEMP)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, COLUMN_CONV_TEMP, INTEGER, "0"));
        }
        if (!columnExists(db, CONVERSATION_TABLE, COLUMN_CONV_TEMP_TTL)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, COLUMN_CONV_TEMP_TTL, NUMBERIC));
        }
      } catch (Exception ex) {
        LOGGER.w("failed to execute upgrade instrument. cause: " + ex.getMessage());
      }
    }

    private static boolean columnExists(SQLiteDatabase db, String table, String column) {
      try {
        Cursor cursor = db.query(table, null, null, null, null, null, null);
        return cursor.getColumnIndex(column) != -1;
      } catch (Exception e) {
        return false;
      }
    }
  }

  private DBHelper dbHelper;
  private String clientId;

  public AndroidDatabaseDelegate(Context context, String clientId) {
    this.dbHelper = new DBHelper(context, clientId);
    this.clientId = clientId;
  }

  private ContentValues transferMap(Map<String, Object> attrs) {
    ContentValues values = new ContentValues();
    for (Map.Entry<String, Object> entry: attrs.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Long) {
        values.put(entry.getKey(), (Long) value);
      } else if (value instanceof String) {
        values.put(entry.getKey(), (String) value);
      } else if (value instanceof Byte) {
        values.put(entry.getKey(), (Byte) value);
      } else if (value instanceof Integer) {
        values.put(entry.getKey(), (Integer) value);
      } else if (value instanceof Float) {
        values.put(entry.getKey(), (Float) value);
      } else if (value instanceof Double) {
        values.put(entry.getKey(), (Double) value);
      } else if (value instanceof Boolean) {
        values.put(entry.getKey(), (Boolean) value);
      } else if (value instanceof byte[]) {
        values.put(entry.getKey(), (byte[]) value);
      }
    }
    return values;
  }

  public int update(String table, Map<String, Object> attrs, String whereClause, String[] whereArgs) {
    try {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      ContentValues values = transferMap(attrs);
      return db.update(table, values, whereClause, whereArgs);
    } catch (Exception ex) {
      LOGGER.w("failed to execute update instrument. cause: " + ex.getMessage());
      return 0;
    }
  }

  public int delete(String table, String whereClause, String[] whereArgs) {
    try {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      return db.delete(table, whereClause, whereArgs);
    } catch (Exception ex) {
      LOGGER.w("failed to execute delete instrument. cause: " + ex.getMessage());
      return 0;
    }
  }

  public int insert(String table, Map<String, Object> attrs) {
    try {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      ContentValues values = transferMap(attrs);
      return (int)db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    } catch (Exception ex) {
      LOGGER.w("failed to execute insert instrument. cause: " + ex.getMessage());
      return -1;
    }
  }
  public int queryCount(String table, String[] columns, String selection,
                 String[] selectionArgs, String groupBy, String having,
                 String orderBy) {
    int resultCount = 0;
    try {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
      resultCount = cursor.getCount();
      cursor.close();
    } catch (Exception e) {
      LOGGER.w("failed to execute count query. cause: " + e.getMessage());
    }
    return resultCount;
  }

  public long countForQuery(String query, String[] selectionArgs) {
    if (StringUtil.isEmpty(query)) {
      return 0l;
    }
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    return DatabaseUtils.longForQuery(db, query, selectionArgs);
  }

  public AVIMMessageStorage.MessageQueryResult queryMessages(String[] columns, String selection, String[] selectionArgs,
                                                             String groupBy, String having, String orderBy, String limit) {
    List<AVIMMessage> resultMessage = new ArrayList<>();
    List<Boolean> resultBreakpoint = new ArrayList<>();
    try {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      Cursor cursor = db.query(MESSAGE_TABLE, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
      boolean breakpoint = false;

      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          AVIMMessage message = createMessageFromCursor(cursor);
          breakpoint = cursor.getInt(cursor.getColumnIndex(COLUMN_BREAKPOINT)) != 0;
          resultMessage.add(message);
          resultBreakpoint.add(breakpoint);
          cursor.moveToNext();
        }
      }
      cursor.close();
    } catch (Exception e) {
      LOGGER.w("failed to execute message query. cause: " + e.getMessage());
    }

    AVIMMessageStorage.MessageQueryResult result = new AVIMMessageStorage.MessageQueryResult();
    result.messages = resultMessage;
    result.breakpoints = resultBreakpoint;
    return result;
  }

  private AVIMMessage createMessageFromCursor(Cursor cursor) {
    String mid = cursor.getString(cursor.getColumnIndex(AVIMMessageStorage.COLUMN_MESSAGE_ID));
    long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
    String cid = cursor.getString(cursor.getColumnIndex(COLUMN_CONVERSATION_ID));
    String from = cursor.getString(cursor.getColumnIndex(COLUMN_FROM_PEER_ID));
    long deliveredAt = cursor.getLong(cursor.getColumnIndex(COLUMN_MESSAGE_DELIVEREDAT));
    long readAt = cursor.getLong(cursor.getColumnIndex(COLUMN_MESSAGE_READAT));
    long updateAt = cursor.getLong(cursor.getColumnIndex(COLUMN_MESSAGE_UPDATEAT));
    byte[] payload = cursor.getBlob(cursor.getColumnIndex(COLUMN_PAYLOAD));
    String uniqueToken = cursor.getString(cursor.getColumnIndex(COLUMN_DEDUPLICATED_TOKEN));
    int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
    int mentionAll = cursor.getInt(cursor.getColumnIndex(COLUMN_MSG_MENTION_ALL));
    String mentionListStr = cursor.getString(cursor.getColumnIndex(COLUMN_MSG_MENTION_LIST));
    int innerType = cursor.getInt(cursor.getColumnIndex(COLUMN_MSG_INNERTYPE));

    AVIMMessage message = null;
    if (innerType == MESSAGE_INNERTYPE_BIN) {
      message = new AVIMBinaryMessage(cid, from, timestamp, deliveredAt, readAt);
      ((AVIMBinaryMessage)message).setBytes(payload);
    } else {
      message = new AVIMMessage(cid, from, timestamp, deliveredAt, readAt);
      message.setContent(new String(payload));
    }
    message.setMessageId(mid);
    message.setUniqueToken(uniqueToken);
    message.setMessageStatus(AVIMMessage.AVIMMessageStatus.getMessageStatus(status));
    message.setUpdateAt(updateAt);
    message.setMentionAll( mentionAll == 1);
    message.setCurrentClient(this.clientId);
    if (!StringUtil.isEmpty(mentionListStr)) {
      message.setMentionListString(mentionListStr);
    }
    return AVIMMessageManager.parseTypedMessage(message);
  }

  public List<AVIMConversation> queryConversations(String[] columns, String selection, String[] selectionArgs,
                                            String groupBy, String having, String orderBy, String limit) {
    List<AVIMConversation> conversations = new LinkedList<AVIMConversation>();
    try {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      Cursor cursor = db.query(CONVERSATION_TABLE, columns, selection, selectionArgs, groupBy, having,
          orderBy, limit);
      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          AVIMConversation conversation = parseConversationFromCursor(cursor);
          conversations.add(conversation);
          cursor.moveToNext();
        }
      }
      cursor.close();
    } catch (Exception e) {
      LOGGER.w("failed to execute conversation query. cause: " + e.getMessage());
    }
    return conversations;
  }

  public List<AVIMConversation> rawQueryConversations(String sql, String[] selectionArgs) {
    List<AVIMConversation> conversations = new LinkedList<AVIMConversation>();
    try {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      Cursor cursor = db.rawQuery(sql, selectionArgs);
      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          AVIMConversation conversation = parseConversationFromCursor(cursor);
          conversations.add(conversation);
          cursor.moveToNext();
        }
      }
      cursor.close();
    } catch (Exception e) {
      LOGGER.w("failed to execute raw query. cause: " + e.getMessage());
    }
    return conversations;
  }

  private AVIMConversation parseConversationFromCursor(Cursor cursor) {
    String conversationId = cursor.getString(cursor.getColumnIndex(COLUMN_CONVERSATION_ID));
    String createdAt = cursor.getString(cursor.getColumnIndex(COLUMN_CREATEDAT));
    String updatedAt = cursor.getString(cursor.getColumnIndex(COLUMN_UPDATEDAT));
    String membersStr = cursor.getString(cursor.getColumnIndex(COLUMN_MEMBERS));
    String attrsStr = cursor.getString(cursor.getColumnIndex(COLUMN_ATTRIBUTE));
    String instanceData = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCEDATA));
    String creator = cursor.getString(cursor.getColumnIndex(COLUMN_CREATOR));
    long lastMessageTS = cursor.getLong(cursor.getColumnIndex(COLUMN_LM));
    int transientValue = cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSIENT));
    int unreadCount = cursor.getInt(cursor.getColumnIndex(COLUMN_UNREAD_COUNT));

    int mentioned = cursor.getInt(cursor.getColumnIndex(COLUMN_CONV_MENTIONED));

    long readAt = cursor.getLong(cursor.getColumnIndex(COLUMN_CONVERSATION_READAT));
    long deliveredAt = cursor.getLong(cursor.getColumnIndex(COLUMN_CONVRESATION_DELIVEREDAT));
    String lastMessage = cursor.getString(cursor.getColumnIndex(COLUMN_LASTMESSAGE));
    int lastMessageInnerType = cursor.getInt(cursor.getColumnIndex(COLUMN_CONV_LASTMESSAGE_INNERTYPE));

    int system = cursor.getInt(cursor.getColumnIndex(COLUMN_CONV_SYSTEM));
    int temporary = cursor.getInt(cursor.getColumnIndex(COLUMN_CONV_TEMP));

    AVIMConversation conversation = null;
    if (temporary > 0) {
      conversation = new AVIMTemporaryConversation(AVIMClient.getInstance(clientId), conversationId);
      long tempExpiredAt = cursor.getLong(cursor.getColumnIndex(COLUMN_CONV_TEMP_TTL));
      conversation.setTemporaryExpiredat(tempExpiredAt);
    } else if (system > 0) {
      conversation = new AVIMServiceConversation(AVIMClient.getInstance(clientId), conversationId);
    } else if (transientValue > 0) {
      conversation = new AVIMChatRoom(AVIMClient.getInstance(clientId), conversationId);
    } else {
      conversation = new AVIMConversation(AVIMClient.getInstance(clientId), conversationId);
    }

    conversation.createdAt = createdAt;
    conversation.updatedAt = updatedAt;
    try {
      conversation.members.clear();
      if (!StringUtil.isEmpty(membersStr)) {
        conversation.members.addAll(JSON.parseObject(membersStr, Set.class));
      }

      conversation.attributes.clear();
      if (!StringUtil.isEmpty(attrsStr)) {
        conversation.attributes.putAll(JSON.parseObject(attrsStr, HashMap.class));
      }

      conversation.instanceData.clear();
      if (!StringUtil.isEmpty(instanceData)) {
        conversation.instanceData.putAll(JSON.parseObject(instanceData, HashMap.class));
      }
      if (lastMessageInnerType != MESSAGE_INNERTYPE_BIN) {
        AVIMMessage msg = JSON.parseObject(lastMessage, AVIMMessage.class);
        conversation.lastMessage = msg;
      } else {
        AVIMBinaryMessage binaryMsg = new AVIMBinaryMessage(conversationId, null);// don't care who sent message.
        binaryMsg.setBytes(Base64Decoder.decodeToBytes(lastMessage));
        conversation.lastMessage = binaryMsg;
      }
    } catch (Exception e) {
      LOGGER.w("failed to parse conversation query result. cause: " + e.getMessage());
    }
    conversation.creator = creator;
    conversation.lastMessageAt = new Date(lastMessageTS);
    conversation.unreadMessagesCount = unreadCount;
    conversation.unreadMessagesMentioned = mentioned == 1;
    conversation.lastReadAt = readAt;
    conversation.lastDeliveredAt = deliveredAt;
    return conversation;
  }
}
