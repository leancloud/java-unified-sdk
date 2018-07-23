package cn.leancloud.push;

import cn.leancloud.AVLogger;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.ObjectValueFilter;
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
  private static final AVLogger LOGGER = LogUtil.getLogger(AVInstallation.class);
  private static final String INSTALLATION = "installation";
  private static final String DEVICETYPETAG = "deviceType";
  private static final String CHANNELSTAG = "channel";
  private static final String INSTALLATIONIDTAG = "installationId";
  private static final String INSTALLATION_AVNAME = "_Installation";
  private static final String TIMEZONE = "timeZone";
  public static final String REGISTRATION_ID = "registrationId";
  public static final String VENDOR = "vendor";
  private static String DEFAULT_DEVICETYPE = "android";
  private static volatile AVInstallation currentInstallation;

  static {
    AVObject.registerSubclass(AVInstallation.class);
  }

  public AVInstallation() {
    super(INSTALLATION_AVNAME);
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

  protected static AVInstallation createInstanceFromLocal(String fileName) {
    boolean needWriteback = true;
    File installationFile = new File(AppConfiguration.getImportantFileDir(), fileName);
    if (installationFile.exists()) {
      String json = PersistenceUtil.sharedInstance().readContentFromFile(installationFile);
      if (!StringUtil.isEmpty(json)) {
        if (json.indexOf("{") >= 0) {
          try {
            currentInstallation = new AVInstallation(JSON.parseObject(json, AVInstallation.class));
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
    return defaultTimezone != null ? defaultTimezone.getID() : "unknown";
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
    File installationFile = new File(AppConfiguration.getImportantFileDir(), INSTALLATION);
    String jsonString = JSON.toJSONString(this, ObjectValueFilter.instance,
            SerializerFeature.WriteClassName,
            SerializerFeature.DisableCircularReferenceDetect);
    PersistenceUtil.sharedInstance().saveContentToFile(jsonString, installationFile);
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
    AVQuery<AVInstallation> query = new AVQuery<AVInstallation>(INSTALLATION_AVNAME);
    return query;
  }

  void setInstallationId(String installationId) {
    this.put(INSTALLATIONIDTAG, installationId);
  }
}
