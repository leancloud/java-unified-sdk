# Changelog

Following is change logs for recently release versions, you can refer to [releases page](https://github.com/leancloud/java-unified-sdk/releases) for more details.

## 6.5.4 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- bugfixed: session auto open error due to lack of appid


## 6.5.3 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- always fetch from network at first while lastMessage is necessary in ConversationsQuery.
- optimized for flutter plugin.

## 6.5.2 release

#### Break changes
- None

#### New features
- feat: support iOS push options(environment, topic, team id, request id, notification id)

#### Optimization and fixed bugs
- None


## 6.5.1 release

#### Break changes
- None

#### New features
- split mixpush by provider: hms, xiaomi, oppo, vivo, meizu.

#### Optimization and fixed bugs
- fixed bug: AVObject modification doesn't work within beforeXXX Hook.

## 6.5.0 release

#### Break changes
- remove fastjson-android dependency from android sdk, all libraries use fastjson jvm version now.

#### New features
- support `_conversationAdded/Removed` hooks within engine-core.

#### Optimization and fixed bugs
- fixed bugs within AVFile uploading.

## 6.4.4 release

#### Break changes
- None

#### New features
- AVMixPushManager support to connect HMS with specified huawei appId:
```java
AVMixPushManager.connectHMS(Activity activity, String huaweiAppId);
```

- AVMixPushManager support to register Oppo with specified profile:
```java
AVMixPushManager.registerOppoPush(Context context, String appKey, String appSecret,
                                  String profile, AVOPPOPushAdapter callback)
```

- add `AVOSCloud.enablePrintAllHeaders4Debug(boolean flag)` method to print the whole request(usable for android securely initialization).

#### Optimization and fixed bugs
- upgrade mixpush with latest third-party SDKs:
  - xiaomi: 3.6.9 -> 3.7.5
  - huawei(HMS): 2.6.3.306 -> 4.0.2.300
  - oppo: 1.0.1 -> 2.0.2
  - vivo: 2.3.4 -> 2.9.0.0

- remove duplicated value within AVInstallation.channels

- support customized subclass of AVIMTypedMessage in Kotlin.

## 6.4.3 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- fixed: can't create conversation with signature.

## 6.4.2 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- fixed: cannot auto re-establish websocket connection for app which enable realtime but disable push service.

## 6.4.1 release

#### Break changes
- None

#### New features
- add IMOption to disable auto login for Push:
```java
/**
* 设置是否禁止推送服务的自动 login 请求
* 对于部分应用来说，如果不使用 LeanCloud 推送服务，仅仅只使用了即时通讯服务的话，可以将这个标志设为 true，以避免不必要的网络连接。
*/
AVIMOptions.getGlobalOptions().setDisableAutoLogin4Push(boolean disableAutoLogin4Push);
```

#### Optimization and fixed bugs
- some performance optimizations.

## 6.4.0 release

#### Break changes
- None

#### New features
- enable cache policy for cloud function calling, add following methods to AVCloud:
```java
public static <T> Observable<T> callFunctionWithCacheInBackground(String name, Map<String, Object> params,
    AVQuery.CachePolicy cachePolicy, long maxCacheAge);
public static <T> Observable<T> callRPCWithCacheInBackground(String name, Map<String, Object> params,
    AVQuery.CachePolicy cachePolicy, long maxCacheAge);
```
- support foreground service for PushService:
```java
PushService.setForegroundMode(true, 101, notification);
```
- add IMOption for persisting last notify timestamp or not:
```java
AVIMOptions.getGlobalOptions().setAlwaysRetrieveAllNotification(true);
```

#### Optimization and fixed bugs
- upgrade realtime protocol, auto-close connection under special instruction from server,
 and report android os info within session open request.
- some performance optimizations.

## 6.3.1 release

#### Break changes
- None


#### New features
- enable specify customized PushReceiver for xiaomi/huawei/flyme mix Push. New methods in AVMixPushManager:
```
public static void registerXiaomiPush(Context context, String miAppId, String miAppKey, Class customizedReceiver);
public static void registerXiaomiPush(Context context, String miAppId, String miAppKey, String profile, Class customizedReceiver);
public static void registerXiaomiPush(Context context, String miAppId, String miAppKey, String profile, boolean isInternationalVendor, Class customizedReceiver);

public static void registerHMSPush(Application application, Class customizedReceiver);
public static void registerHMSPush(Application application, String profile, Class customizedReceiver);

public static boolean registerFlymePush(Context context, String flymeId, String flymeKey, String profile, Class customizedReceiver);
public static boolean registerFlymePush(Context context, String flymeId, String flymeKey, Class customizedReceiver);
```

#### Optimization and fixed bugs
- change return value type from long to int for AVIMConversation#getTemporaryExpiredat().
- update Conversation updatedAt field while receiving muted/unmuted response.
- fixed: upload file-based message at frist within AVIMConversation#updateMessage.
- some performance optimizations.


## 6.3.0 release

Happy New Year of the Rat.

#### Break changes
- AVIMConversation changed #addMember/removeMember callback from AVIMConversationCallback to AVIMConversationPartiallySucceededCallback.
- AVIMConversation deprecated #getAttribute/setAttribute, you should use #get/#set instead of.


#### New features
- realtime sdk will throw self-joined/self-left notification to application.
- add onMessageReceiptEx into MessageHandler, application can get operator info than onMessageReceipt interface.

#### Optimization and fixed bugs
- fixed: async call qiniu rest api for retrieving meta data.
- fixed: call AVIMConversationsQuery#findInBackground twice, the second one will return nothing.
- lots of performance optimization.


## 6.2.1 release

re-release for something wrong on sonatype.org.

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- None

## 6.2.0 release

#### Break changes
- AVObject#saveInBackground() will hold new key-value at local in default, it is also possible that the local key-value isn't correct
 if comparing with cloud data. If you need the exactly result after save or update, please use fetchWhenSave option.
 Or you can disable this feature by call:
```$java
AVOSCloud.setAutoMergeOperationDataWhenSave(false)
```

#### New features
- AVIMConversationsQuery add three methods to support more query demand:
```$java
  /**
   * 是否返回成员列表
   * @param isCompact 为 true 的话则不返回，为 false 的话则返回成员列表，默认为 false
   * @return current instance.
   */
  public AVIMConversationsQuery setCompact(boolean isCompact)
  
  /**
   * find temporary conversations in background.
   * @param conversationIds conversation id list.
   * @param callback callback handler.
   */
  public void findTempConversationsInBackground(List<String> conversationIds, final AVIMConversationQueryCallback callback)

  /**
   * direct find with conditions in background.
   * @param where query condition
   * @param sort sort attributes
   * @param skip skip number
   * @param limit result maximum size
   * @param flag query flag:
   *            0 - Normal,
   *            1 - don't need member list within a conversation item,
   *            2 - attach last message data within a conversation item.
   * @param callback callback function.
   */
  public void directFindInBackground(String where, String sort, int skip, int limit, int flag,
                                     final AVIMConversationQueryCallback callback)
```
conversations and directlyfindTempConversationsInBackground

#### Optimization and fixed bugs
- fixed #98: AVObject.saveAll/InBackground(objects) cannot save cascaded files.
- fixed #97: AVQuery.getInBackground(objectId) ignore include keys setting.


## 6.1.9 release

#### Break changes
- None

#### New features
- add AVObject#fetchIfNeededInBackground(String includeKeys) method.
- add new async methods to AVFile:
```java
Observable<byte[]> getDataInBackground();
Observable<InputStream> getDataStreamInBackground();
```

#### Optimization and fixed bugs
- throw Exception while query#getInBackground result is null.
- fixed: AVCloud#call functions don't transfer parameters.

## 6.1.8 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- make AVObject#getDate(key) compatible for Long/String value.
- publish Fastjson 1.1.71.android-leancloud.jar to fix double value parsing bug(such as 5.43D).


## 6.1.7 release

#### Break changes
- None

#### New features
- support rtmClientSign hook for Realtime.
- make Installation cache synchronized with Put/Get operation.

#### Optimization and fixed bugs
- fixed: cannot put Date or AVObject value into Installation while saving.

## 6.1.6 release

#### Break changes
- None

#### New features
- add option to keep file name within download url for method AVFile#saveInBackground():
```
  /**
   * save to cloud backend.
   * @param keepFileName whether keep file name in url or not.
   * @return Observable object.
   */
  public Observable<AVFile> saveInBackground(boolean keepFileName);
```

#### Optimization and fixed bugs
- fixed: AVQuery#deleteInBackground() will block UI thread.


## 6.1.5 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- fixed: can't get objectId of current AVInstallation after saveInBackground.


## 6.1.4 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- fixed: #86 ClassCastException occurred within AVIMConveration#getMemberCount(), #queryBlockedMembers() and #getMutedMembers().


## 6.1.3 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- fixed: #84 Cannot update AVIMConveration's name


## 6.1.2 release

#### Break changes
- None

#### New features
- AVPush support `flowControl`.

#### Optimization and fixed bugs
- None


## 6.1.1 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- bug fixed: AVParcelableObject throw exception on Android bcz JSONType annotation doesn't work.
- bug fixed: AVStatus.statusQuery() can't get the correct data bcz where condition is lack.
- bug fixed: ObjectFieldOperation can't detect circle reference for collection value.

## 6.1.0 release

#### Break changes
- `AVUser#followeeQuery()` 和 `AVUser#followerQuery()` 的结果由 `List<AVUser>` 变为 `List<AVObject>`，开发者需要对结果再次调用 `getAVObject<AVUser>("follower")` or `getAVObject<AVUser>("followee")` 来得到 AVUser 对象。

#### New features
- support securely initialization for Android platform
- support Status API

#### Optimization and fixed bugs
- None

## 6.0.5 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- fixed: #78(第三方登录 failOnNotExist 参数使用报错)

## 6.0.4 release

#### Break changes
- None

#### New features
- support Realtime client online/offline hook in leanengine library.

#### Optimization and fixed bugs
- change http User Agent to format: `LeanCloud-Java-SDK/x.x.x`.
- add try-catch for crash while registerring receiver in PushService, which occurred on very few Android phones.

## 6.0.3 release

#### Break changes
- None

#### New features
- support more notification channel for android(8.0 or above).
  ```
    PushService#createNotificationChannel(context, id, name, description, importance, ...);
  ```
- add AVOSCloud#getServerDateInBackground()
- mixpush support Xiaomi international version.
  ```
    AVMixpushManager#registerXiaomiPush(context, miAppId, miAppKey, profile, isInternationalVendor)
  ```

#### Optimization and fixed bugs
- None


## 6.0.2 release
#### Overview
At this release, we use two versions for Android and Java SDK:
- Android SDKs‘ version is 6.0.2-androidx
- Java SDKs’ version is 6.0.2

for Android SDK, we switch to [AndroidX](https://developer.android.com/jetpack/androidx), out of use Support Libraries no longer.

#### Break changes
- for domestic application, developers must call AVOSCloud.initialize(context, appId, appKey, serverHost) at first instead of AVOSCloud.initialize(context, appId, appKey) on Android platform.

#### New features
- support fulltext search(former In-App Search);

#### Optimization and fixed bugs
- fixed: AVIMAudioMessage with local file doesn't work.
- optimized RTM network status notification and auto re-connection logic.


## 6.0.0 release
#### Break changes
- for domestic application, developers must call AVOSCloud.initialize(context, appId, appKey, serverHost) at first instead of AVOSCloud.initialize(context, appId, appKey) on Android platform.

#### New features
- LiveQuery login command add clientTs parameter.

#### Optimization and fixed bugs
- upgrade dependencies:
  - okhttp: `3.12.1` -> `4.1.1`
  - retrofit: `2.5.0` -> `2.6.1`
  - rxjava2: `2.2.3` -> `2.2.12`
  - rxandroid: `2.1.0` -> `2.1.1`
  - fastjson: `1.2.46` -> `1.2.60`
  - fastjson-android: `1.1.70.android` -> `1.1.71.android`
  - protobuf-java: `3.4.0` -> `3.9.1`
  - java-websocket: `1.3.9` -> `1.4.0`


## 5.0.26 release
#### Break changes
- None

#### New features
- Added AVLiveQueryConnectionHandler interface to monitor connection status changing, it is optional.

#### Optimization and fixed bugs
- fixed: make livequery workable on Android.


## 5.0.25 release
#### Break changes
- AVObject#getUpdatedAt() and AVObject#getCreatedAt() will return a Date instance instead of String, they are compatiable with old sdk.

#### New features
- Added new methods: AVObject#getUpdatedAtString() and AVObject#getCreatedAtString(), they will return the date string directly.

#### Optimization and fixed bugs
- fixed: livequery login request through wss will be executed before sending subscribe request.
- fixed: AVObject deserializer supports parse recursively, so it can correctly parse embeded AVObject more than 3 layers.

## 5.0.24 release

#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- 修复：在 Android 8.1（含）及以上版本中，PushService 被杀死又启动后（心跳检测断线之前），RTM 无法使用的问题

## 5.0.18 release
#### Break changes
- None

#### New features
- None

#### Optimization and fixed bugs
- fixed: JSON parse exception for null value within result map.


## 5.0.11 release
#### Break changes
- None

#### New features
- convert Throwable(HttpException) to Throwable(AVException) while failed to call RxJava API. 


#### Optimization and fixed bugs
- fixed bug: PushService can't start under Android 6.0(or elder);

## 5.0.10 release
#### Break changes
- None

#### New features
- add new instance method to AVUser: 
```
public void getFollowersAndFolloweesInBackground(final FollowersAndFolloweesCallback callback)
```
- add new static method to AVUser in order to compatible with old Android SDK.
```
public static <T extends AVUser> T cast(AVUser user, Class<T> clazz);
public static AVUser becomeWithSessionToken(String sessionToken);
public static <T extends AVUser> T becomeWithSessionToken(String sessionToken, Class<T> clazz);
```

#### Optimization and fixed bugs
- change AVUser#getQuery declaration as following to compatible with old Android SDK:
```
public static AVQuery<AVUser> getQuery()
```


## 5.0.9 release
#### Break changes
- None

#### New features
- add new static method to AVUser: 
```
AVUser.alwaysUseSubUserClass(Class<? extends AVUser> clazz);
```
- add new static method to PushService. If you only use LiveQuery(exclude Push or Realtime), you can invoke this method to starting PushService(establishing websocket connection).
```
PushService.startIfRequired(android.content.Context context);
```

#### Optimization and fixed bugs
- upgrade okhttp.version to 3.12.1;
- fixed: instance created by `AVFile.withFile(name, file)` can't be upload correctly；
- fixed: can't change http/https protocol dynamically while uploading AVFile；

