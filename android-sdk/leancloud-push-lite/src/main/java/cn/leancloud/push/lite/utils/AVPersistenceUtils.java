package cn.leancloud.push.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cn.leancloud.push.lite.AVOSCloud;

public class AVPersistenceUtils {
  private static final String TAG = AVPersistenceUtils.class.getSimpleName();

  private static final String INSTALLATION = "installation";
  private static AVPersistenceUtils instance = null;
  private static String currentAppPrefix = "";

  private static ConcurrentHashMap<String, ReentrantReadWriteLock> fileLocks =
      new ConcurrentHashMap<String, ReentrantReadWriteLock>();

  public static ReentrantReadWriteLock getLock(String path) {
    ReentrantReadWriteLock lock = fileLocks.get(path);
    if (lock == null) {
      lock = new ReentrantReadWriteLock();
      ReentrantReadWriteLock oldLock = fileLocks.putIfAbsent(path, lock);
      if (oldLock != null) {
        lock = oldLock;
      }
    }
    return lock;
  }

  public static void removeLock(String path) {
    fileLocks.remove(path);
  }

  private AVPersistenceUtils() {}

  public static synchronized AVPersistenceUtils sharedInstance() {
    if (instance == null) {
      instance = new AVPersistenceUtils();
    }

    return instance;
  }

  public static void initAppInfo(String appId, Context ctx) {
    if (StringUtil.isEmpty(appId) || null == ctx) {
      return;
    }
    currentAppPrefix = appId.substring(0, 8);

//    // cache data migration
//    File oldDocumentDir = getPaasDocumentDir_old();
//    File newDocumentDir = getPaasDocumentDir();
//    if (!newDocumentDir.exists() && oldDocumentDir.exists()) {
//      boolean ret = oldDocumentDir.renameTo(newDocumentDir);
//    }
//
//    File oldCommandCacheDir = getCommandCacheDir_old();
//    File newCommandCacheDir = getCommandCacheDir();
//    if (!newCommandCacheDir.exists() && oldCommandCacheDir.exists()) {
//      boolean ret = oldCommandCacheDir.renameTo(newCommandCacheDir);
//    }

    File oldInstallationFile = getInstallationFile_old(ctx);
    File newInstallationFile = getInstallationFile(ctx);
    if (oldInstallationFile.exists() && !newInstallationFile.exists()) {
      boolean ret = oldInstallationFile.renameTo(newInstallationFile);
      if (!ret) {
        Log.w(TAG, "failed to rename installation file. old=" + oldInstallationFile.getAbsolutePath()
            + ", new=" + newInstallationFile.getAbsolutePath());
      }
    }
  }

  public static String getCurrentAppPrefix() {
    return currentAppPrefix;
  }

//  public static File getPaasDocumentDir() {
//    if (AVOSCloud.applicationContext == null) {
//      throw new IllegalStateException(
//          "applicationContext is null, Please call AVOSCloud.initialize first");
//    }
//    return AVOSCloud.applicationContext.getDir(currentAppPrefix + "Paas", Context.MODE_PRIVATE);
//  }
//
//  public static File getCacheDir() {
//    if (AVOSCloud.applicationContext == null) {
//      throw new IllegalStateException(
//          "applicationContext is null, Please call AVOSCloud.initialize first");
//    }
//    return AVOSCloud.applicationContext.getCacheDir();
//  }
//
//  public static File getCommandCacheDir() {
//    if (AVOSCloud.applicationContext == null) {
//      throw new IllegalStateException(
//          "applicationContext is null, Please call AVOSCloud.initialize first");
//    }
//    File dir = new File(getCacheDir(), currentAppPrefix + "CommandCache");
//    dir.mkdirs();
//    return dir;
//  }

  public static File getInstallationFile(Context ctx) {
    return new File(getFilesDir(ctx), currentAppPrefix + INSTALLATION);
  }

  private static File getInstallationFile_old(Context ctx) {
    return new File(getFilesDir(ctx),  INSTALLATION);
  }

  private static File getFilesDir(Context ctx) {
    Context validContext = (null != ctx)? ctx : AVOSCloud.applicationContext;
    if (validContext == null) {
      throw new IllegalStateException(
          "applicationContext is null, Please call AVOSCloud.initialize first");
    }
    return validContext.getFilesDir();
  }

//  public static File getAnalysisCacheDir() {
//    if (AVOSCloud.applicationContext == null) {
//      throw new IllegalStateException(
//          "applicationContext is null, Please call AVOSCloud.initialize first");
//    }
//    File dir = new File(getCacheDir(), currentAppPrefix + "Analysis");
//    dir.mkdirs();
//    return dir;
//  }
//
//  @Deprecated
//  private static File getPaasDocumentDir_old() {
//    if (AVOSCloud.applicationContext == null) {
//      throw new IllegalStateException(
//          "applicationContext is null, Please call AVOSCloud.initialize first");
//    }
//    return AVOSCloud.applicationContext.getDir("Paas", Context.MODE_PRIVATE);
//  }
//
//  @Deprecated
//  private static File getCommandCacheDir_old() {
//    if (AVOSCloud.applicationContext == null) {
//      throw new IllegalStateException(
//          "applicationContext is null, Please call AVOSCloud.initialize first");
//    }
//    File dir = new File(getCacheDir(), "CommandCache");
//    dir.mkdirs();
//    return dir;
//  }

  public static void closeQuietly(Closeable closeable) {
    try {
      if (closeable != null) closeable.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

//  static private File getFile(String folderName, String fileName) {
//    File file;
//    if (StringUtil.isEmpty(folderName)) {
//      file = new File(getPaasDocumentDir(), fileName);
//    } else {
//      File folder = new File(getPaasDocumentDir(), folderName);
//      if (!folder.exists()) {
//        folder.mkdirs();
//      }
//      file = new File(folder, fileName);
//    }
//    return file;
//  }

  // ================================================================================
  // Save to File
  // ================================================================================

//  public void saveToDocumentDir(String content, String fileName) {
//    saveToDocumentDir(content, null, fileName);
//  }
//
//  public void saveToDocumentDir(String content, String folderName, String fileName) {
//    File fileForSave = getFile(folderName, fileName);
//    saveContentToFile(content, fileForSave);
//  }

  public static boolean saveContentToFile(String content, File fileForSave) {
    try {
      return saveContentToFile(content.getBytes("utf-8"), fileForSave);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean saveContentToFile(byte[] content, File fileForSave) {
    Lock writeLock = getLock(fileForSave.getAbsolutePath()).writeLock();
    boolean succeed = true;
    FileOutputStream out = null;
    if (writeLock.tryLock()) {
      try {

        out = new FileOutputStream(fileForSave, false);
        out.write(content);

      } catch (Exception e) {
        e.printStackTrace();
        succeed = false;
      } finally {
        if (out != null) {
          closeQuietly(out);
        }
        writeLock.unlock();
      }
    }
    return succeed;
  }

  // ================================================================================
  // Read from file
  // ================================================================================

//  public String getFromDocumentDir(String folderName, String fileName) {
//    File fileForRead = getFile(folderName, fileName);
//    return readContentFromFile(fileForRead);
//  }

//  public String getFromDocumentDir(String fileName) {
//    return getFromDocumentDir(null, fileName);
//  }

  public static String readContentFromFile(File fileForRead) {
    byte[] data = readContentBytesFromFile(fileForRead);
    if (data == null || data.length == 0) {
      return "";
    } else {
      return new String(data);
    }
  }

  public static InputStream getInputStreamFromFile(File fileForRead) throws IOException{
    if (fileForRead == null) {
      return null;
    };
    if (!fileForRead.exists() || !fileForRead.isFile()) {
      return null;
    }
    return new BufferedInputStream(new FileInputStream(fileForRead), 8192);
  }

  public static byte[] readContentBytesFromFile(File fileForRead) {
    if (fileForRead == null) {
      return null;
    };
    if (!fileForRead.exists() || !fileForRead.isFile()) {
      return null;
    }
    Lock readLock = getLock(fileForRead.getAbsolutePath()).readLock();
    readLock.lock();
    byte[] data = null;
    InputStream input = null;
    try {
      data = new byte[(int) fileForRead.length()];
      int totalBytesRead = 0;
      input = new BufferedInputStream(new FileInputStream(fileForRead), 8192);
      while (totalBytesRead < data.length) {
        int bytesRemaining = data.length - totalBytesRead;
        int bytesRead = input.read(data, totalBytesRead, bytesRemaining);
        if (bytesRead > 0) {
          totalBytesRead = totalBytesRead + bytesRead;
        }
      }
      return data;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      closeQuietly(input);
      readLock.unlock();
    }
    return null;
  }

  // added by tangxm, persistentSettings
  public void savePersistentSettingBoolean(String keyzone, String key, Boolean value) {
    if (AVOSCloud.applicationContext == null) {
      return;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putBoolean(key, value);
    editor.commit();
  }

  public boolean getPersistentSettingBoolean(String keyzone, String key) {
    return getPersistentSettingBoolean(keyzone, key, false);
  }

  public Map<String, ?> getPersistentSetting(String keyzone) {
    if (AVOSCloud.applicationContext == null) {
      return null;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    return settings.getAll();
  }

  public boolean getPersistentSettingBoolean(String keyzone, String key, Boolean defaultValue) {
    if (AVOSCloud.applicationContext == null) {
      return defaultValue;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    return settings.getBoolean(key, defaultValue);
  }

  public void savePersistentSettingInteger(String keyzone, String key, Integer value) {
    if (AVOSCloud.applicationContext == null) {
      return;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putInt(key, value);
    editor.commit();

  }

  public Integer getPersistentSettingInteger(String keyzone, String key, Integer defaultValue) {
    if (AVOSCloud.applicationContext == null) {
      return defaultValue;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    return settings.getInt(key, defaultValue);
  }

  public void savePersistentSettingLong(String keyzone, String key, Long value) {
    if (AVOSCloud.applicationContext == null) {
      return;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putLong(key, value);
    editor.commit();

  }

  public Long getPersistentSettingLong(String keyzone, String key, Long defaultValue) {
    if (AVOSCloud.applicationContext == null) {
      return defaultValue;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    return settings.getLong(key, defaultValue);
  }

  public void savePersistentSettingString(String keyzone, String key, String value) {
    if (AVOSCloud.applicationContext == null) {
      return;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(key, value);
    editor.commit();
  }

  public String getPersistentSettingString(String keyzone, String key, String defaultValue) {
    if (AVOSCloud.applicationContext == null) {
      return defaultValue;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    return settings.getString(key, defaultValue);
  }

  public void removePersistentSettingString(String keyzone, String key) {
    if (null == AVOSCloud.applicationContext) {
      return;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.remove(key);
    editor.commit();
  }

  public String removePersistentSettingString(String keyzone, String key, String defaultValue) {
    if (null == AVOSCloud.applicationContext) {
      return defaultValue;
    }
    String currentValue = getPersistentSettingString(keyzone, key, defaultValue);
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.remove(key);
    editor.commit();
    return currentValue;
  }

  public void removeKeyZonePersistentSettings(String keyzone) {
    if (null == AVOSCloud.applicationContext) {
      return;
    }
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(keyzone, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.clear();
    editor.commit();
  }
}
