package cn.leancloud.oppo;

import android.content.Context;
import java.util.List;

import cn.leancloud.AVException;
import cn.leancloud.AVInstallation;
import cn.leancloud.AVLogger;
import cn.leancloud.AVOPPOPushAdapter;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 16/6/27.
 */
public class AVMixPushManager {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVMixPushManager.class);

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
   * @return
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         AVOPPOPushAdapter callback) {
    if (!isSupportOppoPush(context)) {
      return false;
    }
    com.heytap.mcssdk.PushManager.getInstance().register(context, appKey, appSecret, callback);
    return true;
  }

  /**
   * register oppo push
   * @param context context
   * @param appKey oppo application key
   * @param appSecret oppo application secret
   * @param profile profile string.
   * @param callback callback.
   * @return
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         String profile,
                                         AVOPPOPushAdapter callback) {
    oppoDeviceProfile = profile;
    return registerOppoPush(context, appKey, appSecret, callback);
  }


  /**
   * judgement if support oppo push or not.
   *
   * @param context
   * @return
   */
  public static boolean isSupportOppoPush(Context context) {
    return com.heytap.mcssdk.PushManager.isSupportPush(context);
  }

  /**
   * pause oppo push
   */
  public static void pauseOppoPush() {
    com.heytap.mcssdk.PushManager.getInstance().pausePush();
  }

  /**
   * resume oppo push
   */
  public static void resumeOppoPush() {
    com.heytap.mcssdk.PushManager.getInstance().resumePush();
  }

  /**
   * set oppo push time.
   * @param weekDays
   * @param startHour
   * @param startMinute
   * @param endHour
   * @param endMinute
   */
  public static void setOppoPushTime(List<Integer> weekDays, int startHour, int startMinute,
                                     int endHour, int endMinute) {
    com.heytap.mcssdk.PushManager.getInstance().setPushTime(weekDays, startHour, startMinute,
        endHour, endMinute);
  }

  /**
   * set oppo push aliases.
   * @param aliases
   */
  public static void setOppoAliases(List<String> aliases) {
    com.heytap.mcssdk.PushManager.getInstance().setAliases(aliases);
  }

  /**
   * unset oppo push aliases.
   * @param alias
   */
  public static void unsetOppoAlias(String alias) {
    com.heytap.mcssdk.PushManager.getInstance().unsetAlias(alias);
  }

  /**
   * get oppo aliases.
   */
  public static void getOppoAliases() {
    com.heytap.mcssdk.PushManager.getInstance().getAliases();
  }

  /**
   * set oppo push account.
   * @param account
   */
  public static void setOppoUserAccount(String account) {
    com.heytap.mcssdk.PushManager.getInstance().setUserAccount(account);
  }

  /**
   * unset oppo push accounts.
   * @param accounts
   */
  public static void unsetOppoUserAccouts(List<String> accounts) {
    com.heytap.mcssdk.PushManager.getInstance().unsetUserAccounts(accounts);
  }

  /**
   * get oppo push accounts.
   */
  public static void getOppoUserAccounts() {
    com.heytap.mcssdk.PushManager.getInstance().getUserAccounts();
  }

  /**
   * set oppo push tags.
   * @param tags
   */
  public static void setOppoTags(List<String> tags) {
    com.heytap.mcssdk.PushManager.getInstance().setTags(tags);
  }

  /**
   * unset oppo push tags.
   * @param tags
   */
  public static void unsetOppoTags(List<String> tags) {
    com.heytap.mcssdk.PushManager.getInstance().unsetTags(tags);
  }

  /**
   * retrieve oppo push tags.
   */
  public static void getOppoTags() {
    com.heytap.mcssdk.PushManager.getInstance().getTags();
  }

  /**
   * get oppo push status
   */
  public static void getOppoPushStatus() {
    com.heytap.mcssdk.PushManager.getInstance().getPushStatus();
  }

  /**
   * get oppo notification status.
   */
  public static void getOppoNotificationStatus() {
    com.heytap.mcssdk.PushManager.getInstance().getNotificationStatus();
  }

  /**
   * 取消混合推送的注册
   * 取消成功后，消息会通过 LeanCloud websocket 发送
   */
  public static void unRegisterMixPush() {
    AVInstallation installation = AVInstallation.getCurrentInstallation();
    String vendor = installation.getString(AVInstallation.VENDOR);
    if (!StringUtil.isEmpty(vendor)) {
      installation.put(AVInstallation.VENDOR, "lc");
      installation.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
        @Override
        public void done(AVException e) {
          if (null != e) {
            printErrorLog("unRegisterMixPush error!");
          } else {
            LOGGER.d("Registration canceled successfully!");
          }
        }
      }));
    }
  }

  private static void printErrorLog(String error) {
    if (!StringUtil.isEmpty(error)) {
      LOGGER.e(error);
    }
  }
}
