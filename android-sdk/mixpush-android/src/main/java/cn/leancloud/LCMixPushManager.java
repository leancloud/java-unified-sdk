package cn.leancloud;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.List;

import cn.leancloud.callback.LCCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 16/6/27.
 */
public class LCMixPushManager {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCMixPushManager.class);

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey) {
    cn.leancloud.mi.LCMixPushManager.registerXiaomiPush(context, miAppId, miAppKey);
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param customizedReceiver 自定义 receiver
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey,
                                        Class customizedReceiver) {
    cn.leancloud.mi.LCMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, customizedReceiver);
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param profile  小米推送配置
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey, String profile) {
    cn.leancloud.mi.LCMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, profile);
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param profile  小米推送配置
   * @param customizedReceiver 自定义 receiver
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey, String profile,
                                        Class customizedReceiver) {
    cn.leancloud.mi.LCMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, profile, customizedReceiver);
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param profile  小米推送配置
   * @param isInternationalVendor  是否为小米国际版设备
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey,
                                        String profile, boolean isInternationalVendor) {
    cn.leancloud.mi.LCMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, profile, isInternationalVendor);
  }

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   * @param profile  小米推送配置
   * @param isInternationalVendor  是否为小米国际版设备
   * @param customizedReceiver 自定义 receiver
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey,
                                        String profile, boolean isInternationalVendor, Class customizedReceiver) {
    cn.leancloud.mi.LCMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, profile,
        isInternationalVendor, customizedReceiver);
  }

  /**
   * 注册荣耀推送
   * @param application 应用上下文
   */
  public static void registerHonorPush(Context application) {
    cn.leancloud.honor.LCMixPushManager.registerHonorPush(application);
  }

  /**
   * 注册荣耀推送
   * @param application 应用上下文
   * @param profile 推送配置
   */
  public static void registerHonorPush(Context application, String profile) {
    cn.leancloud.honor.LCMixPushManager.registerHonorPush(application, profile);
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   *
   * @param application 应用实例
   */
  public static void registerHMSPush(Application application) {
    cn.leancloud.hms.LCMixPushManager.registerHMSPush(application);
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   *
   * @param application 应用实例
   * @param profile 华为推送配置
   */
  public static void registerHMSPush(Application application, String profile) {
    cn.leancloud.hms.LCMixPushManager.registerHMSPush(application, profile);
  }

  public static void registerHMSPush(Application application, String profile, Class customMessageServiceClazz) {
    cn.leancloud.hms.LCMixPushManager.registerHMSPush(application, profile, customMessageServiceClazz);
  }

  /**
   *  Connecting to the HMS SDK may pull up the activity (including upgrade guard, etc.), and it is
   *  recommended that you connect in the first activity.
   *  This method can be called repeatedly, and there is no need to do complex processing
   *  for only one call at a time
   *  Method is called asynchronously, and the result is invoked in the main thread callback
   *
   * @param activity activity
   */
  public static void connectHMS(Activity activity) {
    cn.leancloud.hms.LCMixPushManager.connectHMS(activity);
  }

  /**
   *  Connecting to the HMS SDK may pull up the activity (including upgrade guard, etc.), and it is
   *  recommended that you connect in the first activity.
   *  This method can be called repeatedly, and there is no need to do complex processing
   *  for only one call at a time
   *  Method is called asynchronously, and the result is invoked in the main thread callback
   *
   * @param activity activity
   * @param huaweiAppId huawei app id
   */
  public static void connectHMS(Activity activity, String huaweiAppId) {
    cn.leancloud.hms.LCMixPushManager.connectHMS(activity, huaweiAppId);
  }

  /**
   * 开启华为 HMS 推送
   *
   * @param context context
   * @param callback callback function
   */
  public static void turnOnHMSPush(Context context, LCCallback<Void> callback) {
    cn.leancloud.hms.LCMixPushManager.turnOnHMSPush(context, callback);
  }

  /**
   * 关闭华为 HMS 推送
   *
   * @param context context
   * @param callback callback function
   */
  public static void turnOffHMSPush(Context context, LCCallback<Void> callback) {
    cn.leancloud.hms.LCMixPushManager.turnOffHMSPush(context, callback);
  }

  /**
   * 注册魅族推送
   * @param context 上下文
   * @param flymeId flyme app id
   * @param flymeKey flyme app key
   * @param profile 魅族推送配置
   * @return true - register succeed
   *         false - register failed
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey,
                                          String profile) {
    return cn.leancloud.flyme.LCMixPushManager.registerFlymePush(context, flymeId, flymeKey, profile);
  }

  /**
   * 注册魅族推送
   * @param context 上下文
   * @param flymeId flyme app id
   * @param flymeKey flyme app key
   * @param profile 魅族推送配置
   * @param customizedReceiver 自定义 receiver
   * @return true - register succeed
   *         false - register failed
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey,
                                          String profile, Class customizedReceiver) {
    return cn.leancloud.flyme.LCMixPushManager.registerFlymePush(context, flymeId, flymeKey,
        profile, customizedReceiver);
  }

  /**
   * 注册魅族推送
   * @param context 上下文
   * @param flymeId flyme app id
   * @param flymeKey flyme app key
   * @return true - register succeed
   *         false - register failed
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey) {
    return cn.leancloud.flyme.LCMixPushManager.registerFlymePush(context, flymeId, flymeKey);
  }

  /**
   * 注册魅族推送
   * @param context 上下文
   * @param flymeId flyme app id
   * @param flymeKey flyme app key
   * @param customizedReceiver 自定义 receiver
   * @return true - register succeed
   *         false - register failed
   */
  public static boolean registerFlymePush(Context context, String flymeId, String flymeKey,
                                          Class customizedReceiver) {
    return cn.leancloud.flyme.LCMixPushManager.registerFlymePush(context, flymeId, flymeKey,
        customizedReceiver);
  }

  /**
   * set flyme MStatus bar icon.
   *
   * @param icon icon resource id.
   */
  public static void setFlymeMStatusbarIcon(int icon) {
    cn.leancloud.flyme.LCMixPushManager.setFlymeMStatusbarIcon(icon);
  }

  /**
   * VIVO push
   */

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application application
   */
  public static boolean registerVIVOPush(Application application) {
    return cn.leancloud.vivo.LCMixPushManager.registerVIVOPush(application);
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application application
   */
  public static boolean registerVIVOPush(Application application, String profile) {
    return cn.leancloud.vivo.LCMixPushManager.registerVIVOPush(application, profile);
  }

  /**
   * turn off VIVO push.
   */
  public static void turnOffVIVOPush(final LCCallback<Boolean> callback) {
    cn.leancloud.vivo.LCMixPushManager.turnOffVIVOPush(callback);
  }

  /**
   * turn on VIVO push.
   */
  public static void turnOnVIVOPush(final LCCallback<Boolean> callback) {
    cn.leancloud.vivo.LCMixPushManager.turnOnVIVOPush(callback);
  }

  /**
   * current device support VIVO push or not.
   *
   * @param context context
   * @return
   */
  public static boolean isSupportVIVOPush(Context context) {
    return cn.leancloud.vivo.LCMixPushManager.isSupportVIVOPush(context);
  }

  /**
   * bind vivo alias
   *
   * @param context context
   * @param alias alias
   * @param callback callback function
   */
  public static void bindVIVOAlias(Context context, String alias, final LCCallback<Boolean> callback) {
    cn.leancloud.vivo.LCMixPushManager.bindVIVOAlias(context, alias, callback);
  }

  /**
   * unbind vivo alias
   *
   * @param context context
   * @param alias alias
   * @param callback callback function
   */
  public static void unbindVIVOAlias(Context context, String alias, final LCCallback<Boolean> callback) {
    cn.leancloud.vivo.LCMixPushManager.unbindVIVOAlias(context, alias, callback);
  }

  /**
   * get vivo alias
   *
   * @param context context
   * @return alias
   */
  public static String getVIVOAlias(Context context) {
    return cn.leancloud.vivo.LCMixPushManager.getVIVOAlias(context);
  }

  /**
   * set vivo topic
   *
   * @param context context
   * @param topic topic
   * @param callback callback function
   */
  public static void setVIVOTopic(Context context, String topic, final LCCallback<Boolean> callback) {
    cn.leancloud.vivo.LCMixPushManager.setVIVOTopic(context, topic, callback);
  }

  /**
   * delete vivo topic
   * @param context context
   * @param alias alias
   * @param callback callback function
   */
  public static void delVIVOTopic(Context context, String alias, final LCCallback<Boolean> callback) {
    cn.leancloud.vivo.LCMixPushManager.delVIVOTopic(context, alias, callback);
  }

  /**
   * get vivo topics
   * @param context context
   * @return topic list.
   */
  public static List<String> getVIVOTopics(Context context) {
    return cn.leancloud.vivo.LCMixPushManager.getVIVOTopics(context);
  }

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
   * @return boolean: true - succeed, false - failed.
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         LCOPPOPushAdapter callback) {
    return cn.leancloud.oppo.LCMixPushManager.registerOppoPush(context, appKey, appSecret, callback);
  }

  /**
   * register oppo push
   * @param context context
   * @param appKey oppo application key
   * @param appSecret oppo application secret
   * @param profile profile string.
   * @param callback callback.
   * @return boolean: true - succeed, false - failed.
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         String profile,
                                         LCOPPOPushAdapter callback) {
    return cn.leancloud.oppo.LCMixPushManager.registerOppoPush(context, appKey, appSecret, profile, callback);
  }


  /**
   * judgement if support oppo push or not.
   *
   * @param context context
   * @return boolean: true - succeed, false - failed.
   */
  public static boolean isSupportOppoPush(Context context) {
    return cn.leancloud.oppo.LCMixPushManager.isSupportOppoPush(context);
  }

  /**
   * pause oppo push
   */
  public static void pauseOppoPush() {
    cn.leancloud.oppo.LCMixPushManager.pauseOppoPush();
  }

  /**
   * resume oppo push
   */
  public static void resumeOppoPush() {
    cn.leancloud.oppo.LCMixPushManager.resumeOppoPush();
  }

  /**
   * set oppo push time.
   * @param weekDays week days
   * @param startHour start hour
   * @param startMinute start minute
   * @param endHour end hour
   * @param endMinute end minute
   */
  public static void setOppoPushTime(List<Integer> weekDays, int startHour, int startMinute,
                                     int endHour, int endMinute) {
    cn.leancloud.oppo.LCMixPushManager.setOppoPushTime(weekDays, startHour, startMinute, endHour, endMinute);
  }

  /**
   * set oppo push aliases.
   * @param aliases alias list.
   */
  public static void setOppoAliases(List<String> aliases) {
    cn.leancloud.oppo.LCMixPushManager.setOppoAliases(aliases);
  }

  /**
   * unset oppo push aliases.
   * @param alias alias
   */
  public static void unsetOppoAlias(String alias) {
    cn.leancloud.oppo.LCMixPushManager.unsetOppoAlias(alias);
  }

  /**
   * get oppo aliases.
   */
  public static void getOppoAliases() {
    cn.leancloud.oppo.LCMixPushManager.getOppoAliases();
  }

  /**
   * set oppo push account.
   * @param account oppo account
   */
  public static void setOppoUserAccount(String account) {
    cn.leancloud.oppo.LCMixPushManager.setOppoUserAccount(account);
  }

  /**
   * unset oppo push accounts.
   * @param accounts oppo account list.
   */
  public static void unsetOppoUserAccouts(List<String> accounts) {
    cn.leancloud.oppo.LCMixPushManager.unsetOppoUserAccouts(accounts);
  }

  /**
   * get oppo push accounts.
   */
  public static void getOppoUserAccounts() {
    cn.leancloud.oppo.LCMixPushManager.getOppoUserAccounts();
  }

  /**
   * set oppo push tags.
   * @param tags tag list.
   */
  public static void setOppoTags(List<String> tags) {
    cn.leancloud.oppo.LCMixPushManager.setOppoTags(tags);
  }

  /**
   * unset oppo push tags.
   * @param tags tag list.
   */
  public static void unsetOppoTags(List<String> tags) {
    cn.leancloud.oppo.LCMixPushManager.unsetOppoTags(tags);
  }

  /**
   * retrieve oppo push tags.
   */
  public static void getOppoTags() {
    cn.leancloud.oppo.LCMixPushManager.getOppoTags();
  }

  /**
   * get oppo push status
   */
  public static void getOppoPushStatus() {
    cn.leancloud.oppo.LCMixPushManager.getOppoPushStatus();
  }

  /**
   * get oppo notification status.
   */
  public static void getOppoNotificationStatus() {
    cn.leancloud.oppo.LCMixPushManager.getOppoNotificationStatus();
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
    }
  }

  private static void printErrorLog(String error) {
    if (!StringUtil.isEmpty(error)) {
      LOGGER.e(error);
    }
  }
}
