package cn.leancloud;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.List;

import cn.leancloud.callback.AVCallback;
import cn.leancloud.callback.SaveCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

/**
 * Created by wli on 16/6/27.
 */
public class AVMixPushManager {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVMixPushManager.class);

  /**
   * 注册小米推送
   * 只有 appId、appKey 有效 且 MIUI 且 manifest 正确填写 才能注册
   *
   * @param context 上下文
   * @param miAppId 小米 appId
   * @param miAppKey 小米 appKey
   */
  public static void registerXiaomiPush(Context context, String miAppId, String miAppKey) {
    cn.leancloud.mi.AVMixPushManager.registerXiaomiPush(context, miAppId, miAppKey);
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
    cn.leancloud.mi.AVMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, customizedReceiver);
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
    cn.leancloud.mi.AVMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, profile);
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
    cn.leancloud.mi.AVMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, profile, customizedReceiver);
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
    cn.leancloud.mi.AVMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, profile, isInternationalVendor);
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
    cn.leancloud.mi.AVMixPushManager.registerXiaomiPush(context, miAppId, miAppKey, profile,
        isInternationalVendor, customizedReceiver);
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   *
   * @param application 应用实例
   */
  public static void registerHMSPush(Application application) {
    cn.leancloud.hms.AVMixPushManager.registerHMSPush(application);
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   *
   * @param application 应用实例
   * @param profile 华为推送配置
   */
  public static void registerHMSPush(Application application, String profile) {
    cn.leancloud.hms.AVMixPushManager.registerHMSPush(application, profile);
  }

  public static void registerHMSPush(Application application, String profile, Class customMessageServiceClazz) {
    cn.leancloud.hms.AVMixPushManager.registerHMSPush(application, profile, customMessageServiceClazz);
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
    cn.leancloud.hms.AVMixPushManager.connectHMS(activity);
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
    cn.leancloud.hms.AVMixPushManager.connectHMS(activity, huaweiAppId);
  }

  /**
   * 开启华为 HMS 推送
   *
   * @param context context
   * @param callback callback function
   */
  public static void turnOnHMSPush(Context context, AVCallback<Void> callback) {
    cn.leancloud.hms.AVMixPushManager.turnOnHMSPush(context, callback);
  }

  /**
   * 关闭华为 HMS 推送
   *
   * @param context context
   * @param callback callback function
   */
  public static void turnOffHMSPush(Context context, AVCallback<Void> callback) {
    cn.leancloud.hms.AVMixPushManager.turnOffHMSPush(context, callback);
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
    return cn.leancloud.flyme.AVMixPushManager.registerFlymePush(context, flymeId, flymeKey, profile);
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
    return cn.leancloud.flyme.AVMixPushManager.registerFlymePush(context, flymeId, flymeKey,
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
    return cn.leancloud.flyme.AVMixPushManager.registerFlymePush(context, flymeId, flymeKey);
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
    return cn.leancloud.flyme.AVMixPushManager.registerFlymePush(context, flymeId, flymeKey,
        customizedReceiver);
  }

  /**
   * set flyme MStatus bar icon.
   *
   * @param icon icon resource id.
   */
  public static void setFlymeMStatusbarIcon(int icon) {
    cn.leancloud.flyme.AVMixPushManager.setFlymeMStatusbarIcon(icon);
  }

  /**
   * VIVO push
   */

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application application
   */
  public static boolean registerVIVOPush(Application application) {
    return cn.leancloud.vivo.AVMixPushManager.registerVIVOPush(application);
  }

  /**
   * 初始化方法，建议在 Application onCreate 里面调用
   * @param application application
   */
  public static boolean registerVIVOPush(Application application, String profile) {
    return cn.leancloud.vivo.AVMixPushManager.registerVIVOPush(application, profile);
  }

  /**
   * turn off VIVO push.
   */
  public static void turnOffVIVOPush(final AVCallback<Boolean> callback) {
    cn.leancloud.vivo.AVMixPushManager.turnOffVIVOPush(callback);
  }

  /**
   * turn on VIVO push.
   */
  public static void turnOnVIVOPush(final AVCallback<Boolean> callback) {
    cn.leancloud.vivo.AVMixPushManager.turnOnVIVOPush(callback);
  }

  /**
   * current device support VIVO push or not.
   *
   * @param context
   * @return
   */
  public static boolean isSupportVIVOPush(Context context) {
    return cn.leancloud.vivo.AVMixPushManager.isSupportVIVOPush(context);
  }

  /**
   * bind vivo alias
   *
   * @param context
   * @param alias
   * @param callback
   */
  public static void bindVIVOAlias(Context context, String alias, final AVCallback<Boolean> callback) {
    cn.leancloud.vivo.AVMixPushManager.bindVIVOAlias(context, alias, callback);
  }

  /**
   * unbind vivo alias
   *
   * @param context
   * @param alias
   * @param callback
   */
  public static void unbindVIVOAlias(Context context, String alias, final AVCallback<Boolean> callback) {
    cn.leancloud.vivo.AVMixPushManager.unbindVIVOAlias(context, alias, callback);
  }

  /**
   * get vivo alias
   *
   * @param context
   * @return
   */
  public static String getVIVOAlias(Context context) {
    return cn.leancloud.vivo.AVMixPushManager.getVIVOAlias(context);
  }

  /**
   * set vivo topic
   *
   * @param context
   * @param topic
   * @param callback
   */
  public static void setVIVOTopic(Context context, String topic, final AVCallback<Boolean> callback) {
    cn.leancloud.vivo.AVMixPushManager.setVIVOTopic(context, topic, callback);
  }

  /**
   * delete vivo topic
   * @param context
   * @param alias
   * @param callback
   */
  public static void delVIVOTopic(Context context, String alias, final AVCallback<Boolean> callback) {
    cn.leancloud.vivo.AVMixPushManager.delVIVOTopic(context, alias, callback);
  }

  /**
   * get vivo topics
   * @param context
   * @return
   */
  public static List<String> getVIVOTopics(Context context) {
    return cn.leancloud.vivo.AVMixPushManager.getVIVOTopics(context);
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
   * @return
   */
  public static boolean registerOppoPush(Context context, String appKey, String appSecret,
                                         AVOPPOPushAdapter callback) {
    return cn.leancloud.oppo.AVMixPushManager.registerOppoPush(context, appKey, appSecret, callback);
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
    return cn.leancloud.oppo.AVMixPushManager.registerOppoPush(context, appKey, appSecret, profile, callback);
  }


  /**
   * judgement if support oppo push or not.
   *
   * @param context
   * @return
   */
  public static boolean isSupportOppoPush(Context context) {
    return cn.leancloud.oppo.AVMixPushManager.isSupportOppoPush(context);
  }

  /**
   * pause oppo push
   */
  public static void pauseOppoPush() {
    cn.leancloud.oppo.AVMixPushManager.pauseOppoPush();
  }

  /**
   * resume oppo push
   */
  public static void resumeOppoPush() {
    cn.leancloud.oppo.AVMixPushManager.resumeOppoPush();
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
    cn.leancloud.oppo.AVMixPushManager.setOppoPushTime(weekDays, startHour, startMinute, endHour, endMinute);
  }

  /**
   * set oppo push aliases.
   * @param aliases
   */
  public static void setOppoAliases(List<String> aliases) {
    cn.leancloud.oppo.AVMixPushManager.setOppoAliases(aliases);
  }

  /**
   * unset oppo push aliases.
   * @param alias
   */
  public static void unsetOppoAlias(String alias) {
    cn.leancloud.oppo.AVMixPushManager.unsetOppoAlias(alias);
  }

  /**
   * get oppo aliases.
   */
  public static void getOppoAliases() {
    cn.leancloud.oppo.AVMixPushManager.getOppoAliases();
  }

  /**
   * set oppo push account.
   * @param account
   */
  public static void setOppoUserAccount(String account) {
    cn.leancloud.oppo.AVMixPushManager.setOppoUserAccount(account);
  }

  /**
   * unset oppo push accounts.
   * @param accounts
   */
  public static void unsetOppoUserAccouts(List<String> accounts) {
    cn.leancloud.oppo.AVMixPushManager.unsetOppoUserAccouts(accounts);
  }

  /**
   * get oppo push accounts.
   */
  public static void getOppoUserAccounts() {
    cn.leancloud.oppo.AVMixPushManager.getOppoUserAccounts();
  }

  /**
   * set oppo push tags.
   * @param tags
   */
  public static void setOppoTags(List<String> tags) {
    cn.leancloud.oppo.AVMixPushManager.setOppoTags(tags);
  }

  /**
   * unset oppo push tags.
   * @param tags
   */
  public static void unsetOppoTags(List<String> tags) {
    cn.leancloud.oppo.AVMixPushManager.unsetOppoTags(tags);
  }

  /**
   * retrieve oppo push tags.
   */
  public static void getOppoTags() {
    cn.leancloud.oppo.AVMixPushManager.getOppoTags();
  }

  /**
   * get oppo push status
   */
  public static void getOppoPushStatus() {
    cn.leancloud.oppo.AVMixPushManager.getOppoPushStatus();
  }

  /**
   * get oppo notification status.
   */
  public static void getOppoNotificationStatus() {
    cn.leancloud.oppo.AVMixPushManager.getOppoNotificationStatus();
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
