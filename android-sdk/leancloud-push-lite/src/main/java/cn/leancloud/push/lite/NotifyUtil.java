package cn.leancloud.push.lite;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import cn.leancloud.push.lite.utils.AVPersistenceUtils;
import cn.leancloud.push.lite.utils.Base64Decoder;
import cn.leancloud.push.lite.utils.Base64Encoder;
import cn.leancloud.push.lite.utils.StringUtil;

public class NotifyUtil {
  private static final String TAG = NotifyUtil.class.getSimpleName();
  protected static HandlerThread thread = new HandlerThread("com.avos.avoscloud.notify");
  static final int SERVICE_RESTART = 1024;
  static final String SERVICE_RESTART_ACTION = "com.avos.avoscloud.notify.action";
  static {
    thread.start();
  }

  static Handler notifyHandler = new Handler(thread.getLooper()) {
    @Override
    public void handleMessage(Message m) {
      if (m.what == SERVICE_RESTART && AVOSCloud.applicationContext != null) {
        this.removeMessages(SERVICE_RESTART);
        try {
          Set<String> registeredApps = getRegisteredApps();
          for (String encodedAppPackage : registeredApps) {
            String appPackage = Base64Decoder.decode(encodedAppPackage);
            if (!AVOSCloud.applicationContext.getPackageName().equals(appPackage)) {
              Intent intent = new Intent();
              intent.setClassName(appPackage, PushService.class.getName());
              intent.setAction(SERVICE_RESTART_ACTION);
              if (AVOSCloud.showInternalDebugLog()) {
                Log.d(TAG, "try to start:" + appPackage + " from:"
                    + AVOSCloud.applicationContext.getPackageName());
              }
              try {
                AVOSCloud.applicationContext.startService(intent);
              } catch (Exception ex) {
                Log.e(TAG, "failed to startService. cause: " + ex.getMessage());
              }
            }
          }
        } catch (Exception e) {

        }
        registerApp();
      }
    }
  };

  private static void registerApp() {
    Set<String> appSet = getRegisteredApps();
    if (appSet != null) {
      appSet.add(Base64Encoder.encode(AVOSCloud.applicationContext.getPackageName()));
      AVPersistenceUtils.sharedInstance().saveContentToFile(JSON.toJSONString(appSet),
          getRegisterAppsFile());
    }
  }

  private static Set<String> getRegisteredApps() {
    if (AVOSCloud.applicationContext == null) {
      return null;
    }
    File registerFile = getRegisterAppsFile();
    Set<String> appSet = new HashSet<String>();
    if (registerFile.exists()) {
      String registerApps = AVPersistenceUtils.sharedInstance().readContentFromFile(registerFile);
      if (!StringUtil.isEmpty(registerApps)) {
        // catch parse Exception
        try {
          appSet.addAll(JSON.parseObject(registerApps, Set.class));
        } catch (Exception e) {
          if (AVOSCloud.showInternalDebugLog()) {
            Log.e(TAG, "getRegisteredApps", e);
          }
        }
        return appSet;
      }
    }
    return appSet;
  }

  private static File getRegisterAppsFile() {
    File file =
        new File(Environment.getExternalStorageDirectory() + "/Android/data/leancloud/",
            "dontpanic.cp");
    if (file.exists()) {
      return file;
    } else {
      File folder =
          new File(Environment.getExternalStorageDirectory() + "/Android/data/leancloud/");
      folder.mkdirs();
      return file;
    }
  }
}
