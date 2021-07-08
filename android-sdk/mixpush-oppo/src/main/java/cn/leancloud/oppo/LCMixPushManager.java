package cn.leancloud.oppo;

import android.content.Context;
import java.util.List;

import cn.leancloud.LCException;
import cn.leancloud.LCInstallation;
import cn.leancloud.LCLogger;
import cn.leancloud.LCOPPOPushAdapter;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

import com.heytap.msp.push.HeytapPushManager;

/**
 * Created by wli on 16/6/27.
 */
public class LCMixPushManager {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCMixPushManager.class);

  public static final String MIXPUSH_PROFILE = "deviceProfile";

  public static String oppoDeviceProfile = "";

  /**
   * Oppo push
   */

  /**
   * register Oppo Push.
   *
   * @param context context
   * @param appKey oppo application key
   * @param appSecret oppo application secret
   * @param callback callback
   * @return true or false
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         LCOPPOPushAdapter callback) {
    if (null == context || StringUtil.isEmpty(appKey) || StringUtil.isEmpty(appSecret)) {
      LOGGER.e("invalid parameter. context=" + context + ", appKey=" + appKey);
      return false;
    }
    HeytapPushManager.init(context, true);
    if (!isSupportOppoPush(context)) {
      LOGGER.e("current device doesn't support OPPO Push.");
      return false;
    }
    HeytapPushManager.register(context, appKey, appSecret, callback);
    HeytapPushManager.requestNotificationPermission();
    return true;
  }

  /**
   * register oppo push
   * @param context context
   * @param appKey oppo application key
   * @param appSecret oppo application secret
   * @param profile profile string.
   * @param callback callback.
   * @return true or false
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         String profile, LCOPPOPushAdapter callback) {
    oppoDeviceProfile = profile;
    return registerOppoPush(context, appKey, appSecret, callback);
  }


  /**
   * judgement if support oppo push or not.
   *
   * @param context context
   * @return boolean
   */
  public static boolean isSupportOppoPush(Context context) {
    return HeytapPushManager.isSupportPush();
  }

  /**
   * pause oppo push
   */
  public static void pauseOppoPush() {
    HeytapPushManager.pausePush();
  }

  /**
   * resume oppo push
   */
  public static void resumeOppoPush() {
    HeytapPushManager.resumePush();
  }

  /**
   * set oppo push time.
   * @param weekDays weekDays
   * @param startHour start hour
   * @param startMinute start minute
   * @param endHour end hour
   * @param endMinute end minute
   */
  public static void setOppoPushTime(List<Integer> weekDays, int startHour, int startMinute,
                                     int endHour, int endMinute) {
    HeytapPushManager.setPushTime(weekDays, startHour, startMinute,
        endHour, endMinute);
  }

  /**
   * set oppo push aliases.
   * @param aliases aliases
   */
  @Deprecated
  public static void setOppoAliases(List<String> aliases) {
  }

  /**
   * unset oppo push aliases.
   * @param alias alias
   */
  @Deprecated
  public static void unsetOppoAlias(String alias) {
  }

  /**
   * get oppo aliases.
   */
  @Deprecated
  public static void getOppoAliases() {
  }

  /**
   * set oppo push account.
   * @param account oppo account
   */
  @Deprecated
  public static void setOppoUserAccount(String account) {
  }

  /**
   * unset oppo push accounts.
   * @param accounts oppo account list.
   */
  @Deprecated
  public static void unsetOppoUserAccouts(List<String> accounts) {
  }

  /**
   * get oppo push accounts.
   */
  @Deprecated
  public static void getOppoUserAccounts() {
  }

  /**
   * set oppo push tags.
   * @param tags tag list.
   */
  @Deprecated
  public static void setOppoTags(List<String> tags) {
  }

  /**
   * unset oppo push tags.
   * @param tags tag list.
   */
  @Deprecated
  public static void unsetOppoTags(List<String> tags) {
  }

  /**
   * retrieve oppo push tags.
   */
  @Deprecated
  public static void getOppoTags() {
  }

  /**
   * get oppo push status
   */
  public static void getOppoPushStatus() {
    HeytapPushManager.getPushStatus();
  }

  /**
   * get oppo notification status.
   */
  public static void getOppoNotificationStatus() {
    HeytapPushManager.getNotificationStatus();
  }

  /**
   * 取消混合推送的注册
   * 取消成功后，消息会通过 LeanCloud websocket 发送
   */
  public static void unRegisterMixPush() {
    LCInstallation installation = LCInstallation.getCurrentInstallation();
    String vendor = installation.getString(LCInstallation.VENDOR);
    if (!StringUtil.isEmpty(vendor)) {
      installation.put(LCInstallation.VENDOR, "lc");
      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(LCException e) {
          if (null != e) {
            printErrorLog("unRegisterMixPush error!");
          } else {
            LOGGER.d("Registration canceled successfully!");
          }
        }
      }));
      HeytapPushManager.unRegister();
    }
  }

  private static void printErrorLog(String error) {
    if (!StringUtil.isEmpty(error)) {
      LOGGER.e(error);
    }
  }
}
