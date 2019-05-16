package cn.leancloud.demo.push.lite.demo;

import android.app.Application;
import android.util.Log;

import cn.leancloud.push.lite.AVCallback;
import cn.leancloud.push.lite.AVException;
import cn.leancloud.push.lite.AVInstallation;
import cn.leancloud.push.lite.AVOSCloud;
import cn.leancloud.push.lite.PushService;

public class MyApp extends Application {
  private String appId = "Gvv2k8PugDTmYOCfuK8tiWd8-gzGzoHsz";
  private String appKey = "dpwAo94n81jPsHVxaWwdxJVu";

  @Override
  public void onCreate() {
    super.onCreate();

    AVOSCloud.setLogLevel(AVOSCloud.LOG_LEVEL_DEBUG);
    AVOSCloud.initialize(this, appId, appKey);

    final AVInstallation curInstall = AVInstallation.getCurrentInstallation();

    curInstall.saveInBackground(new AVCallback<Void>() {
      protected void internalDone0(Void t, AVException ex) {
        if (ex == null) {
          // 保存成功
          String installationId = curInstall.getInstallationId();
          // 关联  installationId 到用户表等操作……
          System.out.println("succeed to save installation with id: " + installationId);
        } else {
          // 保存失败，输出错误信息
          System.out.println("failed to save installation. cause:" + ex.getMessage());
        }
      }
    });
    PushService.setDefaultPushCallback(this, MainActivity.class);
    PushService.subscribe(this, "public", MainActivity.class);
  }
}
