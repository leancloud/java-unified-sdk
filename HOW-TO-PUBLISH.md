# 如何发布新版本

我们现在已经将 Java Unified SDK 全部推送到了 maven 中央仓库（https://oss.sonatype.org/）。要发布 SDK 新版本，总体上需要两步：
1. 发布 JVM 版本的 SDK；
2. 发布 Android 版本的 SDK（依赖上一步的结果）。

SDK 目录结构如下：
```
java-unified-sdk(root directory)
  |-- core（存储服务核心 SDK）
  |-- realtime（RTM 服务核心 SDK）
  |-- leanengine（云引擎 SDK）
  |-- android-sdk
        |-- storage-android（增加 android 适配的存储 SDK）
        |-- realtime-android（增加 android 适配的 RTM SDK）
        |-- mixpush-xiaomi
        |-- mixpush-vivo
        |-- mixpush-oppo
        |-- mixpush-meizu
        |-- mixpush-hms
        |-- mixpush-honor
        |-- mixpush-android（All-in-one 的混合推送 SDK）
        |-- mixpush-fcm
```

下面我们以发布新版本（10.0.0）为目标，来演示一下发布流程。

### 准备工作
在发布过程中，需要做如下准备：
1. 在本地 maven 全局设置文件（路径：$HOME/.m2/settings.xml）中增加 maven 中央仓库的账号信息（目前是统一使用一套账户）。
```
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>jfeng</username>
      <password>{place-with-correct-password}</password>
    </server>
  </servers>
</settings>
```
在本地 gradle 全局设置文件（路径：$HOME/.gradle/gradle.properties）中增加 maven 中央仓库的账号信息与 GPG 签名配置。
```
NEXUS_USERNAME=jfeng
NEXUS_PASSWORD={place-with-correct-password}

signing.keyId=
signing.password=
signing.secretKeyRingFile=
```
增加环境变量
```
export GPG_PASSPHRASE={signing.password}
```
导入 GPG 私钥
```
gpg --allow-secret-key-import --import /path/to/secring.gpg
```
2. 修改 root directory 下的 `CHANGELOG.md` 文件，增加新版本说明。
3. 修改 android-sdk 目录下的 build.gradle 文件，将 `sdkVersion = "8.2.19"` 改为要发布的新版本（修改 Android SDK 依赖的基础 SDK 版本号）。
4. 修改 android-sdk 目录下的 gradle.properties 文件，将 `VERSION_NAME=8.2.19` 改为要发布的新版本（修改 Android SDK 自己的版本号）。
5. git add 以上修改。

## 发布流程
### 发布 JVM 版 SDK
按以下步骤操作即可：
1. 进入 root directory，切换到 master 分支，将本地所有代码提交并与云端同步。
2. 执行命令：`./build-sdk.sh 10.0.0`。

等脚本执行成功，那么新版本 SDK 应该就已上传到 maven 中央仓库，并且以及走完 publish 流程了。与此同时，github 上也多了一个 10.0.0 的 release tag。

### 发布 Android 版 SDK
在 JVM 版发布完成之后，进入 android-sdk 目录，执行如下命令：`./build-sdk.sh 10.0.0`。

- JDK 版本需要为 1.8
- android-sdk 下创建文件 local.properties，内容为 `sdk.dir={path-to-android-sdk}`

等执行成功之后，需要使用同样的账户+密码登录[maven 中央仓库](https://oss.sonatype.org/#stagingRepositories)，手动把刚才上传的 package 执行 close 和 release 两步操作，成功之后 Android SDK 就最终发布了。不过由于 maven 源由 stage 同步到 public 可能还需要一些时间，开发者可能延后 15 分钟左右才能拉取到最新版本。
