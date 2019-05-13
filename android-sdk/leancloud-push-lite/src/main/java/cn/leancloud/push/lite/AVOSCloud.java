package cn.leancloud.push.lite;

import android.content.Context;
import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;

public class AVOSCloud {
  public static Context applicationContext = null;
  public static String applicationId = null;
  public static String clientKey = null;
  protected static Handler handler = null;

  static {
    JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    try {
      Class avInstallationClass = Class.forName("cn.leancloud.push.lite.AVInstallation");
      ParserConfig.getGlobalInstance().putDeserializer(avInstallationClass,
          AVObjectDeserializer.instance);
      SerializeConfig.getGlobalInstance().put(avInstallationClass, AVObjectSerializer.instance);
    } catch (Exception e) {
    }
  }

  public static void initialize(Context context, String applicationId, String clientKey) {
    ;
  }
  public static String getApplicationId() {
    return null;
  }

  public static Context getContext() {
    return null;
  }
}
