package cn.leancloud.push.lite;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cn.leancloud.push.lite.rest.AVHttpClient;
import cn.leancloud.push.lite.utils.AVPersistenceUtils;
import cn.leancloud.push.lite.utils.StringUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AVInstallation implements Parcelable {
  private static final String CLASSNAME = "_Installation";
  private static final String CREATED_AT = "createdAt";
  private static final String UPDATED_AT = "updatedAt";
  private static final String OBJECT_ID = "objectId";

  private static final int UUID_LEN = UUID.randomUUID().toString().length();

  private static final String DEVICETYPETAG = "deviceType";
  private static final String CHANNELSTAG = "channel";
  private static final String INSTALLATIONIDTAG = "installationId";
  public static final String REGISTRATION_ID = "registrationId";
  public static final String VENDOR = "vendor";

  private static final String TAG = AVInstallation.class.getSimpleName();
  private static final JSONObject deleteOP = JSON.parseObject("{\"__op\":\"Delete\"}");
  private static volatile AVInstallation currentInstallation;

  protected String objectId = null;
  protected String updatedAt = null;
  protected String createdAt = null;
  Map<String, Object> serverData = new ConcurrentHashMap<String, Object>();
  Map<String, JSONObject> removedAttr = new ConcurrentHashMap<>();

  public static AVInstallation getCurrentInstallation() {
    return getCurrentInstallation(null);
  }

  public static AVInstallation getCurrentInstallation(Context ctx) {
    Context usingCtx = (null == ctx)? AVOSCloud.applicationContext : ctx;
    if (currentInstallation == null) {
      synchronized (AVInstallation.class) {
        if (currentInstallation == null && readInstallationFile(usingCtx) == null) {
          createNewInstallation(usingCtx);
        }
      }
    }
    return currentInstallation;
  }

  private static void createNewInstallation(Context ctx) {
    String id = genInstallationId(ctx);
    currentInstallation = new AVInstallation();
    currentInstallation.setInstallationId(id);
    currentInstallation.put(INSTALLATIONIDTAG, id);
    saveCurrentInstalationToLocal(ctx);
  }

  private static String genInstallationId(Context ctx) {
    // app的包名
    String packageName = ctx.getPackageName();
    String additionalStr = UUID.randomUUID().toString();
    return StringUtil.md5(packageName + additionalStr);
  }

  private static void saveCurrentInstalationToLocal(Context ctx) {
    try {
      writeInstallationFile(ctx, currentInstallation);
    } catch (Exception e) {
      Log.w(TAG, "failed to save installation cache. cause: " + e.getMessage());
    }
  }

  static private String deviceType() {
    return "android";
  }

  private static String timezone() {
    TimeZone defaultTimezone = TimeZone.getDefault();
    return defaultTimezone != null ? defaultTimezone.getID() : "unknown";
  }

  protected static AVInstallation readInstallationFile(Context usingCtx) {
    if (null == usingCtx) {
      Log.w(TAG, "Context is null, Please call AVOSCloud.initialize at first in Application");
      return null;
    }
    String json = "";
    try {
      File installationFile = AVPersistenceUtils.getInstallationFile(usingCtx);

      if (installationFile.exists()) {
        json = AVPersistenceUtils.readContentFromFile(installationFile);

        if (json.indexOf("{") >= 0) {
          // replace leading type name to compatible with v4.x android sdk serialized json string.
          json = json.replaceAll("^\\{\\s*\"@type\":\\s*\"[A-Za-z\\.]+\",", "{");

          JSONObject installationJson = JSON.parseObject(json, Feature.SupportAutoType);

          currentInstallation = new AVInstallation();
          if (installationJson.containsKey("updatedAt")) {
            currentInstallation.setUpdatedAt(installationJson.getString("updatedAt"));
          }
          if (installationJson.containsKey("objectId")) {
            currentInstallation.setObjectId(installationJson.getString("objectId"));
          }
          if (installationJson.containsKey("createdAt")) {
            currentInstallation.setCreatedAt(installationJson.getString("createdAt"));
          }
          if (installationJson.containsKey("serverData")) {
            JSONObject serverDataJson = installationJson.getJSONObject("serverData");
            if (null != serverDataJson) {
              for (String key : serverDataJson.keySet()) {
                if (key.startsWith("@")) {
                  continue;
                } else {
                  currentInstallation.put(key, serverDataJson.get(key));
                }
              }
            }
          }
        } else {
          if (json.length() == UUID_LEN) {
            // old sdk verson.
            currentInstallation = new AVInstallation();
            currentInstallation.setInstallationId(json);
            // update it
            saveCurrentInstalationToLocal(usingCtx);
          }
        }
        return currentInstallation;
      }
    } catch (Exception e) {
      // try to instance a new installation
      Log.w(TAG, "failed to read installation cache file. cause: " + e.getMessage());
    }
    return null;
  }

  private static void writeInstallationFile(Context ctx, AVInstallation installation) throws IOException {
    if (null != ctx && null != installation) {
      installation.initialize();
      File installationFile = AVPersistenceUtils.getInstallationFile(ctx);
      String jsonString =
          JSON.toJSONString(installation, ObjectValueFilter.instance,
              SerializerFeature.WriteClassName,
              SerializerFeature.DisableCircularReferenceDetect);

      AVPersistenceUtils.saveContentToFile(jsonString, installationFile);
    }
  }

  public AVInstallation() {
    initialize();
  }

  public AVInstallation(Parcel in) {
    String className = in.readString();
    setCreatedAt(in.readString());
    setUpdatedAt(in.readString());
    setObjectId(in.readString());
    String serverDataStr = in.readString();
    Map<String, Object> serverDataMap = (Map<String, Object>) JSON.parse(serverDataStr);
    if (serverDataMap != null && !serverDataMap.isEmpty()) {
      this.serverData.putAll(serverDataMap);
    }
  }

  private void initialize() {
    try {
      if (currentInstallation != null) {
        put(INSTALLATIONIDTAG, currentInstallation.getInstallationId());
      }
      this.put(DEVICETYPETAG, deviceType());
      this.put("timeZone", timezone());
    } catch (IllegalArgumentException exception) {
      Log.w(TAG, "failed to initialize Installation. cause: " + exception.getMessage());
    }
  }

  public String getInstallationId() {
    return getString(INSTALLATIONIDTAG);
  }

  void setInstallationId(String id) {
    this.put(INSTALLATIONIDTAG, id);
  }

  public String getObjectId() {
    return this.objectId;
  }

  public void setObjectId(String id) {
    this.objectId = id;
    this.serverData.put(OBJECT_ID, id);
  }

  Map<String, Object> getServerData() {
    return serverData;
  }

  void setServerData(Map<String, Object> serverData) {
    this.serverData.clear();
    this.serverData.putAll(serverData);
  }

  void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    this.serverData.put(UPDATED_AT, updatedAt);
  }

  void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
    this.serverData.put(CREATED_AT, createdAt);
  }

  public String getClassName() {
    return this.CLASSNAME;
  }

  public void remove(String key) {
    if (StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("key should not be null or empty.");
    }
    this.serverData.remove(key);
    this.removedAttr.put(key, deleteOP);
  }

  public void removeAll(final String key, final Collection<?> values) {
    List val = getList(key);
    if (null != val) {
      val.removeAll(values);
      put(key, val);
    }
  }

  public void put(final String key, final Object value) {
    if (StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("key should not be null or empty.");
    }
    this.serverData.put(key, value);
  }

  public void increment(final String key, final Number amount) {
    if (StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("key should not be null or empty.");
    }
    Number oldValue = getNumber(key);
    if (null == oldValue) {
      oldValue = amount;
    } else {
      oldValue = oldValue.longValue() + amount.longValue();
    }
    put(key, oldValue);
  }

  public void increment(String key) {
    if (StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("key should not be null or empty.");
    }
    this.increment(key, 1);
  }

  public void addUnique(String key, Object value) {
    if (StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("key should not be null or empty.");
    }
    this.addObjectToArray(key, value, true);
  }

  public void addAllUnique(String key, Collection<?> values) {
    if (StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("key should not be null or empty.");
    }
    for (Object item : values) {
      this.addObjectToArray(key, item, true);
    }
  }

  public void addAll(String key, Collection<?> values) {
    if (StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("key should not be null or empty.");
    }
    for (Object item : values) {
      this.addObjectToArray(key, item, false);
    }
  }

  public void add(String key, Object value) {
    if (StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("key should not be null or empty.");
    }
    this.addObjectToArray(key, value, false);
  }

  private void addObjectToArray(final String key, final Object value, final boolean unique) {
    List oldValue = getList(key);
    if (null == oldValue) {
      oldValue = new ArrayList(1);
      oldValue.add(value);
      put(key, oldValue);
    } else {
      boolean needAdd = true;
      if (unique) {
        for (Object obj : oldValue) {
          if (null != obj && obj.equals(value)) {
            needAdd = false;
            break;
          }
        }
      }
      if (needAdd) {
        oldValue.add(value);
        put(key, oldValue);
      }
    }
  }

  public boolean containsKey(String key) {
    return (get(key) != null);
  }

  public Object get(String key) {
    if (CREATED_AT.equals(key)) {
      return getCreatedAt();
    }
    if (UPDATED_AT.equals(key)) {
      return getUpdatedAt();
    }
    return serverData.get(key);
  }

  public boolean getBoolean(String key) {
    Boolean b = (Boolean) get(key);
    return b == null ? false : b;
  }

  public byte[] getBytes(String key) {
    return (byte[]) (get(key));
  }

  public String getString(String key) {
    Object obj = get(key);
    if (obj instanceof String)
      return (String) obj;
    else
      return null;
  }

  public Number getNumber(String key) {
    Number number = (Number) get(key);
    return number;
  }

  public long getLong(String key) {
    Number number = (Number) get(key);
    if (number != null) return number.longValue();
    return 0L;
  }

  public int getInt(String key) {
    Number v = (Number) get(key);
    if (v != null) return v.intValue();
    return 0;
  }

  public Date getDate(String key) {
    return (Date) get(key);
  }

  public double getDouble(String key) {
    Number number = (Number) get(key);
    if (number != null) return number.doubleValue();
    return 0;
  }

  public List getList(String key) {
    return (List) get(key);
  }

  public JSONObject getJSONObject(String key) {
    Object object = get(key);
    if (object instanceof JSONObject) {
      return (JSONObject) object;
    }
    String jsonString = JSON.toJSONString(object);
    JSONObject jsonObject = null;
    try {
      jsonObject = JSON.parseObject(jsonString);
    } catch (Exception exception) {
      throw new IllegalStateException("Invalid json string", exception);
    }
    return jsonObject;
  }

  public JSONArray getJSONArray(String key) {
    Object list = get(key);
    if (list == null) return null;
    if (list instanceof JSONArray) return (JSONArray) list;
    if (list instanceof Collection<?>) {
      JSONArray array = new JSONArray(Arrays.asList((Collection) list));
      return array;
    }
    if (list instanceof Object[]) {
      JSONArray array = new JSONArray();
      for (Object obj : (Object[]) list) {
        array.add(obj);
      }
      return array;
    }
    return null;
  }

  public Date getCreatedAt() {
    return StringUtil.dateFromString(createdAt);
  }

  public Date getUpdatedAt() {
    return StringUtil.dateFromString(updatedAt);
  }

  public static AVInstallation createWithoutData(String objectId) {
    AVInstallation result = new AVInstallation();
    result.setObjectId(objectId);
    return result;
  }

  /**
   * fetch operation
   */
  public void refreshInBackground() {
    refreshInBackground(null);
  }

  public void refreshInBackground(AVCallback<AVInstallation> callback) {
    refreshInBackground(null, callback);
  }

  /**
   * refresh data in background.
   * @param includeKeys project attr names, ignored by Installation.
   * @param callback
   */
  public void refreshInBackground(String includeKeys, AVCallback<AVInstallation> callback) {
    if (StringUtil.isEmpty(this.objectId)) {
      throw new IllegalStateException("objectId is null.");
    }
    AVHttpClient.getInstance().findInstallation(this.objectId, new Callback<JSONObject>() {
      @Override
      public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
        JSONObject result = response.body();
        mergeServerData(result);
        if (null != callback) {
          callback.internalDone(AVInstallation.this, null);
        }
      }

      @Override
      public void onFailure(Call<JSONObject> call, Throwable t) {
        if (null != callback) {
          callback.internalDone(new AVException(t));
        }
      }
    });
  }

  public void fetchInBackground(AVCallback<AVInstallation> callback) {
    refreshInBackground(callback);
  }

  public void fetchInBackground(String includeKeys, AVCallback<AVInstallation> callback) {
    refreshInBackground(includeKeys, callback);
  }

  private void mergeServerData(JSONObject data) {
    if (null == data) {
      return;
    }
    this.serverData.putAll(data.getInnerMap());
    saveCurrentInstalationToLocal(AVOSCloud.applicationContext);
  }

  /**
   * save operation
   */
//  public void deleteEventually() {
//    deleteEventually(null);
//  }
//
//  public void deleteEventually(AVCallback<Void> callback) {
//    if (StringUtil.isEmpty(this.objectId)) {
//      throw new IllegalStateException("objectId is null.");
//    }
//    if (null != callback) {
//
//    }
//  }
//
//  public void deleteInBackground() {
//    this.deleteInBackground(null);
//  }
//
//  public void deleteInBackground(AVCallback<Void> callback) {
//    ;
//  }

  public void saveInBackground() {
    this.saveInBackground(null);
  }

  public void saveInBackground(AVCallback<Void> callback) {
    this.saveInBackground(false, callback);
  }

  public void saveInBackground(boolean fetchWhenSave, AVCallback<Void> callback) {
    JSONObject param = new JSONObject(this.serverData);
    if (param.containsKey(UPDATED_AT)) {
      param.remove(UPDATED_AT);
    }
    if (param.containsKey(CREATED_AT)) {
      param.remove(CREATED_AT);
    }
    if (param.containsKey(OBJECT_ID)) {
      param.putAll(removedAttr);
    } else {
      removedAttr.clear();
    }
    AVHttpClient.getInstance().saveInstallation(param, fetchWhenSave, new Callback<JSONObject>() {
      @Override
      public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
        JSONObject result = response.body();
        mergeServerData(result);
        removedAttr.clear();
        if (null != callback) {
          callback.internalDone(null);
        }
      }

      @Override
      public void onFailure(Call<JSONObject> call, Throwable t) {
        if (null != callback) {
          callback.internalDone(new AVException(t));
        }
      }
    });
  }

  /**
   * Parcel Interface
   */
  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int i) {
    out.writeString(this.CLASSNAME);
    out.writeString(this.createdAt);
    out.writeString(this.updatedAt);
    out.writeString(this.objectId);
    out.writeString(JSON.toJSONString(serverData, new ObjectValueFilter(),
        SerializerFeature.NotWriteRootClassName, SerializerFeature.NotWriteDefaultValue));
  }

  public static final Creator CREATOR = new InstallationCreator();

  /**
   * Creator
   */
  public static class InstallationCreator implements Creator {
    public AVInstallation createFromParcel(Parcel source) {
      return new AVInstallation(source);
    }

    public AVInstallation[] newArray(int size) {
      return new AVInstallation[size];
    }
  }
}
