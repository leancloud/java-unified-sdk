# LeanCloud Java/Android Unified SDK

LeanCloud Unified SDK 包含 LeanCloud 平台全部功能的客户端接口，适用于 Java 和 Android 两个平台。

SDK 中所有存储 API 接口与 LeanCloud 云端交互严格遵循 LeanCloud REST API 规范，并且全部采用了 Reactive 函数式编程风格来设计（同时也兼容老的 Callback 方式）。

要想了解更多使用信息，请查看 [Wiki](https://github.com/leancloud/java-unified-sdk/wiki).

## Development Progress

注意：此 SDK 当前还在开发中，我们会逐步提供全平台的功能。最新进度如下：

### 存储服务 library（Java/Android 两个平台）
- [x] AVObject 增删改查
- [x] AVQuery 与 AVCloudQuery
- [x] AVUser 内建账户系统
- [x] Role 与 ACL
- [x] AVStatus

### 云引擎 library
- [x] EngineFunction
- [x] EngineHook
- [x] IMHook

### 实时通讯核心 library
- [x] LiveQuery
- [x] RTM

### Android 推送和实时通讯 library
- [x] LiveQuery
- [x] RTM
- [x] Push

### Android 混合推送 library
- [x] 华为 HMS 推送
- [x] 小米推送
- [x] 魅族推送
- [x] Firebase Cloud Messaging

## Migration to 8.x

从 8.x 版本开始，我们把公开类名字前缀由 `AV` 改为了 `LC`，同时也删除了一些长期处于 `deprecated` 状态的接口。开发者升级到 8.x 版本，要做的主要改动包括：

1. 将公开类名前缀由 `AV` 改为了 `LC`，例如：

  - `AVDate` -> `LCDate`，`AVClassName` -> `LCClassName`，`AVGeoPoint` -> `LCGeoPoint`
  - `AVObject` -> `LCObject`，`AVUser` -> `LCUser`，`AVFile` -> `LCFile`
  - `AVQuery` -> `LCQuery`，`AVCloud` -> `LCCloud`，
  - `AVException` -> `LCException`
  - `AVOSCloud` -> `LeanCloud`
  - `AVIMClient` -> `LCIMClient`, `AVIMMessage` -> `LCIMMessage`
  - 其他

2. change enum `AVIMMessage#AVIMMessageIOType` to `LCIMMessage#MessageIOType`, and redefined values:
  - `TypeIn`(formal `AVIMMessageIOTypeIn`)
  - `TypeOut`(formal `AVIMMessageIOTypeOut`)

3. change enum `AVIMMessage#AVIMMessageStatus` to `LCIMMessage#MessageStatus`, and redefined values:
  - `StatusNone`(formal `AVIMMessageStatusNone`)
  - `StatusSending`(formal `AVIMMessageStatusSending`)
  - `StatusSent`(formal `AVIMMessageStatusSent`)
  - `StatusReceipt`(formal `AVIMMessageStatusReceipt`)
  - `StatusFailed`(formal `AVIMMessageStatusFailed`)
  - `StatusRecalled`(formal `AVIMMessageStatusRecalled`)

4. change enum `AVIMMessageQueryDirection` to `LCIMMessageQueryDirection` and redefined values:
  - `DirectionUnknown`(formal `AVIMMessageQueryDirectionUnknown`),
  - `DirectionFromNewToOld`(formal `AVIMMessageQueryDirectionFromNewToOld`),
  - `DirectionFromOldToNew`(formal `AVIMMessageQueryDirectionFromOldToNew`);

5. change inner class `AVIMMessageInterval#AVIMMessageIntervalBound` to `LCIMMessageInterval#MessageIntervalBound`.

6. 如果开发者通过 SDK 提供的辅助类 `ObserverBuilder` 将 FindCallback 实例转为了订阅者实例，就需要将原来的 `ObserverBuilder#buildSingleObserver` 变为 `ObserverBuilder#buildCollectionObserver`（因为 `ObserverBuilder#buildSingleObserver(FindCallback)` 已经被移除了）。

7. 如果开发者使用了 CQL 查询，那么需要将原来的 `AVQuery#doCloudQueryInBackground` 调用改为 `LCCloudQuery#executeInBackground`。

## Migration to 7.x
从 7.0.0 版本开始，我们将 Java Unified SDK 底层的 JSON 解析模块完全切换到了 Gson，开发者在业务层使用 Java Unified SDK 与 JSON 解析库，主要有如下三种情形：

1. 业务层并没有特别使用 JSON 解析库，JSON 解析属于 Java Unified SDK 的内部实现细节，一般情况下开发者感知不到这一改变，所以这时候应用层可以无缝切换。
2. 业务代码中因 Java Unified SDK 的原因顺带使用了部分 fastjson 核心类型（例如 JSONObject 和 JSONArray），要切换到最新版就需要去掉这些 fastjson 核心类的使用。出于兼容目的 Java Unified SDK 也提供了完全相同的 API 接口，所以开发者在升级的时候只需要将引用的包名由 `com.alibaba.fastjson` 替换成 `cn.leancloud.json` 即可，例如：

```
//import com.alibaba.fastjson.JSON
//import com.alibaba.fastjson.JSONObject
//import com.alibaba.fastjson.JSONArray

import cn.leancloud.json.JSON
import cn.leancloud.json.JSONObject
import cn.leancloud.json.JSONArray
```
3. 业务层自主使用了 fastjson 解析库，例如访问了 LeanCloud 之外的 REST API Server，强依赖 fastjson 进行了数据解析，此时最好不要升级到新版本（除非能容忍同时引入 fastjson 和 Gson 两套解析框架）。

### 参考 demo：

- 使用存储服务的用户，可以参考 [storage sample app(branch: feat/gson)](https://github.com/leancloud/java-unified-sdk/tree/feat/gson/android-sdk/storage-sample-app);
- 使用即时通讯/推送服务的用户，可以参考 [chatkit-android(branch: feat/gson)](https://github.com/leancloud/LeanCloudChatKit-Android/tree/feat/gson);


### 其他问题：

1. 升级到 `7.0.0-SNAPSHOT` 之后，Android Studio 打包时出现 RuntimeException，出错信息如下：

```
java.lang.RuntimeException
        at org.objectweb.asm.ClassVisitor.visitModule(ClassVisitor.java:148)
        at org.objectweb.asm.ClassReader.readModule(ClassReader.java:731)
        at org.objectweb.asm.ClassReader.accept(ClassReader.java:632)
        at com.google.firebase.perf.plugin.instrumentation.Instrument.instrument(Instrument.java:151)
        at com.google.firebase.perf.plugin.instrumentation.Instrument.instrumentClassesInJar(Instrument.java:100)
```

按照[这里](https://github.com/google/gson/issues/1641)的解释，可以通过升级 `Android Gradle plugin -> 3.5.3, Gradle -> v5.5` 解决。


2. 能不能让开发者配置使用 fastjson 还是 Gson？

我们有计划将 Java Unified SDK 核心代码和 JSON 解析库分开，以后开发者可以根据自己的需求配置使用 Gson 或者 fastjson，类似于 retrofit2 的 converter factory，开发排期则要视开发者的需求而定。

## Bugs and Feedback
大家使用中发现 Bug、或者有任何疑问或建议，请使用  [GitHub Issues](https://github.com/leancloud/java-unified-sdk/issues) 来告知我们，非常感谢大家的反馈。

## License

```
Copyright (c) 2017-present, Meiwei Shuqian(Beijing) Information Technology Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
