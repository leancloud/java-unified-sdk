package cn.leancloud.push;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.codec.Base64Decoder;
import cn.leancloud.codec.Base64Encoder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by fengjunwen on 2018/8/7.
 */

public class NotifyUtil {
  private static AVLogger LOGGER = LogUtil.getLogger(NotifyUtil.class);

  protected static HandlerThread thread = new HandlerThread("com.avos.avoscloud.notify");
  static final int SERVICE_RESTART = 1024;
  static final String SERVICE_RESTART_ACTION = "com.avos.avoscloud.notify.action";
  static {
    thread.start();
  }

  static Handler notifyHandler = new Handler(thread.getLooper()) {
    @Override
    public void handleMessage(Message m) {
      if (m.what == SERVICE_RESTART && AVOSCloud.getContext() != null) {
        this.removeMessages(SERVICE_RESTART);
        try {
          Set<String> registeredApps = getRegisteredApps();
          for (String encodedAppPackage : registeredApps) {
            String appPackage = Base64Decoder.decode(encodedAppPackage);
            if (!AVOSCloud.getContext().getPackageName().equals(appPackage)) {
              Intent intent = new Intent();
              intent.setClassName(appPackage, PushService.class.getName());
              intent.setAction(SERVICE_RESTART_ACTION);
              LOGGER.d("try to start:" + appPackage + " from:"
                  + AVOSCloud.getContext().getPackageName());
              try {
                AVOSCloud.getContext().startService(intent);
              } catch (Exception ex) {
                LOGGER.e("failed to startService. cause: " + ex.getMessage());
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
      appSet.add(Base64Encoder.encode(AVOSCloud.getContext().getPackageName()));
      PersistenceUtil.sharedInstance().saveContentToFile(JSON.toJSONString(appSet),
          getRegisterAppsFile());
    }
  }

  private static Set<String> getRegisteredApps() {
    if (AVOSCloud.getContext() == null) {
      return null;
    }
    File registerFile = getRegisterAppsFile();
    Set<String> appSet = new HashSet<String>();
    if (registerFile.exists()) {
      String registerApps = PersistenceUtil.sharedInstance().readContentFromFile(registerFile);
      if (!StringUtil.isEmpty(registerApps)) {
        // catch parse Exception
        try {
          appSet.addAll(JSON.parseObject(registerApps, Set.class));
        } catch (Exception e) {
          LOGGER.e("getRegisteredApps", e);
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
