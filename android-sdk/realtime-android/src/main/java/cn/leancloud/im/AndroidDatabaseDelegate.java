package cn.leancloud.im;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.Map;

import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageStorage;

import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONVERSATION_READAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONVRESATION_DELIVEREDAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_LASTMESSAGE_INNERTYPE;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_MENTIONED;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_SYSTEM;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_TEMP;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_CONV_TEMP_TTL;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MESSAGE_READAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MESSAGE_UPDATEAT;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MSG_INNERTYPE;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MSG_MENTION_ALL;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_MSG_MENTION_LIST;
import static cn.leancloud.im.v2.AVIMMessageStorage.COLUMN_UNREAD_COUNT;
import static cn.leancloud.im.v2.AVIMMessageStorage.CONVERSATION_TABLE;
import static cn.leancloud.im.v2.AVIMMessageStorage.DB_NAME_PREFIX;
import static cn.leancloud.im.v2.AVIMMessageStorage.INTEGER;
import static cn.leancloud.im.v2.AVIMMessageStorage.MESSAGE_TABLE;
import static cn.leancloud.im.v2.AVIMMessageStorage.NUMBERIC;
import static cn.leancloud.im.v2.AVIMMessageStorage.TEXT;

/**
 * Created by fengjunwen on 2018/8/9.
 */

public class AndroidDatabaseDelegate implements DatabaseDelegate {

  static class DBHelper extends SQLiteOpenHelper {
    static final String MESSAGE_CREATE_SQL =
        "CREATE TABLE IF NOT EXISTS " + MESSAGE_TABLE + " ("
            + AVIMMessageStorage.COLUMN_CONVERSATION_ID + " VARCHAR(32) NOT NULL, "
            + AVIMMessageStorage.COLUMN_MESSAGE_ID + " VARCHAR(32) NOT NULL, "
            + AVIMMessageStorage.COLUMN_TIMESTAMP + " NUMBERIC, "
            + AVIMMessageStorage.COLUMN_FROM_PEER_ID + " TEXT NOT NULL, "
            + AVIMMessageStorage.COLUMN_MESSAGE_DELIVEREDAT + " NUMBERIC, "
            + COLUMN_MESSAGE_READAT + " NUMBERIC, "
            + COLUMN_MESSAGE_UPDATEAT + " NUMBERIC, "
            + AVIMMessageStorage.COLUMN_PAYLOAD + " BLOB, "
            + AVIMMessageStorage.COLUMN_STATUS + " INTEGER, "
            + AVIMMessageStorage.COLUMN_BREAKPOINT + " INTEGER, "
            + AVIMMessageStorage.COLUMN_DEDUPLICATED_TOKEN + " VARCHAR(32), "
            + COLUMN_MSG_MENTION_ALL + " INTEGER default 0, "
            + COLUMN_MSG_MENTION_LIST + " TEXT NULL, "
            + COLUMN_MSG_INNERTYPE + " INTEGER default 0, "
            + "PRIMARY KEY(" + AVIMMessageStorage.COLUMN_CONVERSATION_ID + ","
            + AVIMMessageStorage.COLUMN_MESSAGE_ID + ")) ";

    static final String MESSAGE_UNIQUE_INDEX_SQL =
        "CREATE UNIQUE INDEX IF NOT EXISTS " + AVIMMessageStorage.MESSAGE_INDEX + " on "
            + MESSAGE_TABLE + " (" + AVIMMessageStorage.COLUMN_CONVERSATION_ID
            + ", " + AVIMMessageStorage.COLUMN_TIMESTAMP + ", " + AVIMMessageStorage.COLUMN_MESSAGE_ID + ") ";

    static final String CONVERSATION_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
        + CONVERSATION_TABLE + " ("
        + AVIMMessageStorage.COLUMN_CONVERSATION_ID + " VARCHAR(32) NOT NULL,"
        + AVIMMessageStorage.COLUMN_EXPIREAT + " NUMBERIC,"
        + AVIMMessageStorage.COLUMN_ATTRIBUTE + " BLOB,"
        + AVIMMessageStorage.COLUMN_INSTANCEDATA + " BLOB,"
        + AVIMMessageStorage.COLUMN_UPDATEDAT + " VARCHAR(32),"
        + AVIMMessageStorage.COLUMN_CREATEDAT + " VARCHAR(32),"
        + AVIMMessageStorage.COLUMN_CREATOR + " TEXT,"
        + AVIMMessageStorage.COLUMN_MEMBERS + " TEXT,"
        + AVIMMessageStorage.COLUMN_TRANSIENT + " INTEGER,"
        + COLUMN_UNREAD_COUNT + " INTEGER,"
        + COLUMN_CONVERSATION_READAT + " NUMBERIC,"
        + COLUMN_CONVRESATION_DELIVEREDAT + " NUMBERIC,"
        + AVIMMessageStorage.COLUMN_LM + " NUMBERIC,"
        + AVIMMessageStorage.COLUMN_LASTMESSAGE + " TEXT,"
        + COLUMN_CONV_MENTIONED + " INTEGER default 0,"
        + COLUMN_CONV_LASTMESSAGE_INNERTYPE + " INTEGER default 0, "
        + COLUMN_CONV_SYSTEM + " INTEGER default 0, "
        + COLUMN_CONV_TEMP + " INTEGER default 0, "
        + COLUMN_CONV_TEMP_TTL + " NUMBERIC, "
        + "PRIMARY KEY(" + AVIMMessageStorage.COLUMN_CONVERSATION_ID + "))";

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
        if (!columnExists(db, MESSAGE_TABLE, AVIMMessageStorage.COLUMN_DEDUPLICATED_TOKEN)) {
          db.execSQL(getAddColumnSql(MESSAGE_TABLE,
              AVIMMessageStorage.COLUMN_DEDUPLICATED_TOKEN, AVIMMessageStorage.VARCHAR32));
        }
      } catch (Exception e) {}
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
      try {
        if (!columnExists(db, CONVERSATION_TABLE, AVIMMessageStorage.COLUMN_LASTMESSAGE)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE,
              AVIMMessageStorage.COLUMN_LASTMESSAGE, TEXT));
        }
      } catch (Exception e) {}
    }

    private void upgradeToVersion5(SQLiteDatabase db) {
      try {
        if (!columnExists(db, CONVERSATION_TABLE, AVIMMessageStorage.COLUMN_INSTANCEDATA)) {
          db.execSQL(getAddColumnSql(CONVERSATION_TABLE, AVIMMessageStorage.COLUMN_INSTANCEDATA,
              AVIMMessageStorage.BLOB));
        }
      } catch (Exception e) {
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
      } catch (Exception e) {
      }
    }

    private void upgradeToVersion7(SQLiteDatabase db) {
      try {
        if (!columnExists(db, MESSAGE_TABLE, COLUMN_MESSAGE_UPDATEAT)) {
          db.execSQL(getAddColumnSql(MESSAGE_TABLE, COLUMN_MESSAGE_UPDATEAT, NUMBERIC));
        }
      } catch (Exception e) {
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
      } catch (Exception e) {
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
      } catch (Exception e) {
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
        ;
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
  public int update(String table, Map<String, Object> attrs, String whereClause) {
    return 0;
  }
  public int update(String table, Map<String, Object> attrs, String whereClause, String[] whereArgs) {
    return 0;
  }
  public int delete(String table, String whereClause, String[] whereArgs) {
    return 0;
  }
  public int insert(String table, Map<String, Object> attrs) {
    return 0;
  }
  public int queryCount(String table, String[] columns, String selection,
                 String[] selectionArgs, String groupBy, String having,
                 String orderBy) {
    return 0;
  }
  public List<AVIMMessage> queryMessages(String[] columns, String selection, String[] selectionArgs,
                                  String groupBy, String having, String orderBy, String limit) {
    return null;
  }
  public List<AVIMConversation> queryConversations(String[] columns, String selection, String[] selectionArgs,
                                            String groupBy, String having, String orderBy, String limit) {
    return null;
  }
  public List<AVIMConversation> rawQueryConversations(String sql, String[] selectionArgs) {
    return null;
  }
}
