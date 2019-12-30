package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.codec.MD5;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.File;
import java.util.TimeZone;
import java.util.UUID;

@AVClassName("_Installation")
public final class AVInstallation extends AVObject {
  public static final String CLASS_NAME = "_Installation";

  private static final AVLogger LOGGER = LogUtil.getLogger(AVInstallation.class);
  static final String INSTALLATION = "installation";
  private static final String DEVICETYPETAG = "deviceType";
  private static final String CHANNELSTAG = "channel";
  private static final String INSTALLATIONIDTAG = "installationId";
  private static final String TIMEZONE = "timeZone";
  public static final String REGISTRATION_ID = "registrationId";
  public static final String VENDOR = "vendor";
  private static String DEFAULT_DEVICETYPE = "android";
  private static volatile AVInstallation currentInstallation;

  public AVInstallation() {
    super(CLASS_NAME);
    this.totallyOverwrite = true;
    initialize();
    this.endpointClassName = "installations";
  }

  protected AVInstallation(AVObject obj) {
    this.objectId = obj.getObjectId();
    this.acl = obj.getACL();
    this.serverData = obj.getServerData();
    this.totallyOverwrite = true;
    this.endpointClassName = "installations";
  }

  public static AVInstallation getCurrentInstallation() {
    if (null == currentInstallation) {
      synchronized (AVInstallation.class) {
        if (null == currentInstallation) {
          currentInstallation = createInstanceFromLocal(INSTALLATION);
        }
      }
    }
    return currentInstallation;
  }

  private static File getCacheFile() {
    String cacheBase = AppConfiguration.getImportantFileDir();
    if (StringUtil.isEmpty(cacheBase)) {
      return null;
    }
    return new File(cacheBase, cn.leancloud.core.AVOSCloud.getSimplifiedAppId() + INSTALLATION);
  }

  protected static AVInstallation createInstanceFromLocal(String fileName) {
    boolean needWriteback = true;
    File installationFile = getCacheFile();
    if (null == installationFile) {
      needWriteback = false;
    } else {
      if (!installationFile.exists()) {
        String cacheBase = AppConfiguration.getImportantFileDir();
        File oldInstallationFile = new File(cacheBase, INSTALLATION);
        if (oldInstallationFile.exists() && !installationFile.exists()) {
          boolean tmp = oldInstallationFile.renameTo(installationFile);
          if (!tmp) {
            LOGGER.w("failed to rename installation cache file.");
          }
        }
      }
      if (installationFile.exists()) {
        String json = PersistenceUtil.sharedInstance().readContentFromFile(installationFile);
        if (!StringUtil.isEmpty(json)) {
          if (json.indexOf("{") >= 0) {
            try {
              currentInstallation = (AVInstallation) AVObject.parseAVObject(json);
              currentInstallation.totallyOverwrite = true;
              needWriteback = false;
            } catch (Exception ex) {
              LOGGER.w("failed to parse local installation data.", ex);
              needWriteback = true;
            }
          } else {
            if (json.length() == UUID_LEN) {
              // old sdk version.
              currentInstallation = new AVInstallation();
              currentInstallation.setInstallationId(json);
            }
          }
        }
      }
    }
    if (null == currentInstallation) {
      currentInstallation = new AVInstallation();
    }
    if (needWriteback) {
      String jsonString = JSON.toJSONString(currentInstallation, ObjectValueFilter.instance,
              SerializerFeature.WriteClassName,
              SerializerFeature.DisableCircularReferenceDetect);
      PersistenceUtil.sharedInstance().saveContentToFile(jsonString, installationFile);
    }
    return currentInstallation;
  }

  public static void changeDeviceType(String deviceType) {
    DEFAULT_DEVICETYPE = deviceType;
  }

  private static String deviceType() {
    return DEFAULT_DEVICETYPE;
  }
  private static String timezone() {
    TimeZone defaultTimezone = TimeZone.getDefault();
    return defaultTimezone.getID();
  }

  private void initialize() {
    if (currentInstallation != null) {
      this.put(INSTALLATIONIDTAG, currentInstallation.getInstallationId());
    } else {
      String installationId = genInstallationId();
      if (!StringUtil.isEmpty(installationId)) {
        this.put(INSTALLATIONIDTAG, installationId);
      }
    }
    this.put(DEVICETYPETAG, deviceType());
    this.put(TIMEZONE, timezone());
    File installationFile = getCacheFile();
    if (null != installationFile) {
      String jsonString = JSON.toJSONString(this, ObjectValueFilter.instance,
              SerializerFeature.WriteClassName,
              SerializerFeature.DisableCircularReferenceDetect);
      PersistenceUtil.sharedInstance().saveContentToFile(jsonString, installationFile);
    }
  }

  private static String genInstallationId() {
    // app的包名
    String packageName = AppConfiguration.getApplicationPackagename();
    String additionalStr = UUID.randomUUID().toString();
    return MD5.computeMD5(packageName + additionalStr);
  }

  public String getInstallationId() {
    return this.getString(INSTALLATIONIDTAG);
  }

  public static AVQuery<AVInstallation> getQuery() {
    AVQuery<AVInstallation> query = new AVQuery<AVInstallation>(CLASS_NAME);
    return query;
  }

  @Override
  protected void onSaveSuccess() {
    super.onSaveSuccess();
    updateCurrentInstallationCache();
  }

  @Override
  protected void onDataSynchronized() {
    super.onDataSynchronized();
    updateCurrentInstallationCache();
  }

  void updateCurrentInstallationCache() {
    if (currentInstallation == this) {
      File installationFile = getCacheFile();
      String jsonString = JSON.toJSONString(currentInstallation, ObjectValueFilter.instance,
              SerializerFeature.WriteClassName,
              SerializerFeature.DisableCircularReferenceDetect);
      PersistenceUtil.sharedInstance().saveContentToFile(jsonString, installationFile);
    }
  }

  void setInstallationId(String installationId) {
    this.put(INSTALLATIONIDTAG, installationId);
  }
}
