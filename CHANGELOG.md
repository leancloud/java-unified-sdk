# Changelog

Following is change logs for recently release versions, you can refer to [releases page](https://github.com/leancloud/java-unified-sdk/releases) for more details.

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

