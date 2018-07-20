package cn.leancloud.im;

public class InternalConfiguration {
  private static EventBroadcast eventBroadcast = new SimpleEventBroadcast();
  private static CommandCourier commandCourier = new SimpleCommandCourier();
  private static FileMetaAccessor fileMetaAccessor = new SimpleFileMetaAccessor();
  private static DatabaseDelegate databaseDelegate = null;

  public static EventBroadcast getEventBroadcast() {
    return eventBroadcast;
  }

  public static void setEventBroadcast(EventBroadcast eventBroadcast) {
    InternalConfiguration.eventBroadcast = eventBroadcast;
  }

  public static CommandCourier getCommandCourier() {
    return commandCourier;
  }

  public static void setCommandCourier(CommandCourier commandCarrier) {
    InternalConfiguration.commandCourier = commandCarrier;
  }

  public static FileMetaAccessor getFileMetaAccessor() {
    return fileMetaAccessor;
  }

  public static void setFileMetaAccessor(FileMetaAccessor fileMetaAccessor) {
    InternalConfiguration.fileMetaAccessor = fileMetaAccessor;
  }

  public static DatabaseDelegate getDatabaseDelegate() {
    return databaseDelegate;
  }

  public static void setDatabaseDelegate(DatabaseDelegate databaseDelegate) {
    InternalConfiguration.databaseDelegate = databaseDelegate;
  }
}
