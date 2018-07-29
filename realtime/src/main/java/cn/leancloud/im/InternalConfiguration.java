package cn.leancloud.im;

public class InternalConfiguration {
  private static OperationTube operationTube = new DirectlyOperationTube(true);
  private static FileMetaAccessor fileMetaAccessor = new SimpleFileMetaAccessor();
  private static DatabaseDelegate databaseDelegate = null;

  public static OperationTube getOperationTube() {
    return operationTube;
  }

  public static void setOperationTube(OperationTube commandCarrier) {
    InternalConfiguration.operationTube = commandCarrier;
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
