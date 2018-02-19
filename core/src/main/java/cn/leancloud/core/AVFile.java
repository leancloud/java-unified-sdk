package cn.leancloud.core;

import cn.leancloud.AVException;
import cn.leancloud.ProgressCallback;
import cn.leancloud.codec.MD5;
import cn.leancloud.core.cache.FileCache;
import cn.leancloud.core.cache.PersistenceUtil;
import cn.leancloud.core.ops.ObjectFieldOperation;
import cn.leancloud.core.ops.OperationBuilder;
import cn.leancloud.network.PaasClient;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import cn.leancloud.upload.*;
import cn.leancloud.utils.AVLogger;
import cn.leancloud.utils.FileUtil;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public final class AVFile extends AVObject {
  private static AVLogger logger = LogUtil.getLogger(AVFile.class);
  public static final String CLASS_NAME = "_File";

  private static final String FILE_SUM_KEY = "_checksum";
  private static final String FILE_NAME_KEY = "_name";
  private static final String FILE_LENGTH_KEY = "size";
  private static final String FILE_SOURCE_KEY = "__source";
  private static final String FILE_SOURCE_EXTERNAL = "external";
  private static final String ELDERMETADATAKEYFORIOSFIX = "metadata";

  private static final String THUMBNAIL_FMT = "?imageView/%d/w/%d/h/%d/q/%d/format/%s";

  private static final String KEY_FILE_NAME = "name";
  private static final String KEY_METADATA = "metaData";
  private static final String KEY_URL = "url";
  private static final String KEY_BUCKET = "bucket";
  private static final String KEY_PROVIDER = "provider";
  private static final String KEY_MIME_TYPE = "mime_type";
  private static final String KEY_FILE_KEY = "key";

  public static void setUploadHeader(String key, String value) {
    FileUploader.setUploadHeader(key, value);
  }

  private String localPath = "";
  private String cachePath = "";

  public AVFile() {
    super(CLASS_NAME);
    if (PaasClient.getDefaultACL() != null) {
      this.acl = new AVACL(PaasClient.getDefaultACL());
    }
  }

  public AVFile(String name, byte[] data) {
    this();
    if (null == data) {
      throw new IllegalArgumentException("data is illegal(null)");
    }
    internalPut(KEY_FILE_NAME, name);
    addMetaData(FILE_NAME_KEY, name);
    String md5 = MD5.computeMD5(data);
    localPath = FileCache.getIntance().saveData(md5, data);
    addMetaData(FILE_SUM_KEY, md5);
    addMetaData(FILE_LENGTH_KEY, data.length);
    internalPut(KEY_MIME_TYPE, FileUtil.getMimeTypeFromLocalFile(name));
  }

  public AVFile(String name, File localFile) {
    this();
    if (null == localFile || !localFile.exists() || !localFile.isFile()) {
      throw new IllegalArgumentException("local file is illegal.");
    }
    internalPut(KEY_FILE_NAME, name);
    addMetaData(FILE_NAME_KEY, name);
    String md5 = MD5.computeFileMD5(localFile);
    localPath = localFile.getAbsolutePath();
    addMetaData(FILE_SUM_KEY, md5);
    addMetaData(FILE_LENGTH_KEY, localFile.length());
    internalPut(KEY_MIME_TYPE, FileUtil.getMimeTypeFromLocalFile(localPath));
  }

  public AVFile(String name, String url) {
    this(name, url, null);
  }

  public AVFile(String name, String url, Map<String, Object> metaData) {
    this(name, url, metaData, true);
  }

  protected AVFile(String name, String url, Map<String, Object> metaData, boolean external) {
    this();
    internalPut(KEY_FILE_NAME, name);
    addMetaData(FILE_NAME_KEY, name);
    internalPut(KEY_URL, url);
    Map<String, Object> meta = new HashMap<String, Object>();
    if (null != metaData) {
      meta.putAll(metaData);
    }
    if (external) {
      meta.put(FILE_SOURCE_KEY, FILE_SOURCE_EXTERNAL);
    }
    internalPut(KEY_METADATA, meta);
    internalPut(KEY_MIME_TYPE, FileUtil.getMimeTypeFromUrl(url));
  }

  private Object internalGet(String key) {
    Object value = serverData.get(key);
    ObjectFieldOperation op = operations.get(key);
    if (null != op) {
      value = op.apply(value);
    }
    return value;
  }

  private void internalPut(String key, Object value) {
    ObjectFieldOperation op = OperationBuilder.BUILDER.create(OperationBuilder.OperationType.Set, key, value);
    addNewOperation(op);
  }

  private void internalPutDirectly(String key, Object value) {
    this.serverData.put(key, value);
  }

  public static Observable<AVFile> withObjectIdInBackground(final String objectId) {
    return PaasClient.getStorageClient().fetchFile(objectId);
  }

  public String getName() {
    return (String) internalGet(KEY_FILE_NAME);
  }

  public void setName(String name) {
    internalPut(KEY_FILE_NAME, name);
  }

  public Map<String, Object> getMetaData() {
    return (Map<String, Object>) internalGet(KEY_METADATA);
  }

  public void setMetaData(Map<String, Object> metaData) {
    internalPut(KEY_METADATA, metaData);
  }

  public void addMetaData(String key, Object val) {
    Map<String, Object> metaData = getMetaData();
    metaData.put(key, val);
  }

  public Object getMetaData(String key) {
    return getMetaData().get(key);
  }

  /**
   * Remove file meta data.
   *
   * @param key The meta data's key
   * @return The metadata value.
   * @since 1.3.4
   */
  public Object removeMetaData(String key) {
    return getMetaData().remove(key);
  }

  /**
   * Clear file metadata.
   */
  public void clearMetaData() {
    getMetaData().clear();
  }

  public int getSize() {
    Number size = (Number) getMetaData(FILE_LENGTH_KEY);
    if (size != null)
      return size.intValue();
    else
      return -1;
  }

  public String getMimeType() {
    return (String) internalGet(KEY_MIME_TYPE);
  }

  public void setMimeType(String mimeType) {
    internalPut(KEY_MIME_TYPE, mimeType);
  }

  public static String getMimeType(String url) {
    return FileUtil.getMimeTypeFromUrl(url);
  }

  public String getKey() {
    return (String) internalGet(KEY_FILE_KEY);
  }

  public String getBucket() {
    return (String) internalGet(KEY_BUCKET);
  }

  public void setBucket(String bucket) {
    internalPut(KEY_BUCKET, bucket);
  }

  public String getUrl() {
    return (String) internalGet(KEY_URL);
  }

  @Override
  public void put(String key, Object value) {
    throw new UnsupportedOperationException("cannot invoke put method in AVFile");
  }

  @Override
  public Object get(String key) {
    throw new UnsupportedOperationException("cannot invoke get method in AVFile");
  }

  @Override
  public void remove(String key) {
    throw new UnsupportedOperationException("cannot invoke get method in AVFile");
  }

  @Override
  public void increment(String key) {
    throw new UnsupportedOperationException("cannot invoke get method in AVFile");
  }

  @Override
  public void increment(String key, Number value) {
    throw new UnsupportedOperationException("cannot invoke get method in AVFile");
  }

  /**
   * Returns a thumbnail image url using QiNiu endpoints.
   *
   * @param scaleToFit
   * @param width
   * @param height
   * @return
   * @see #getThumbnailUrl(boolean, int, int, int, String)
   */
  public String getThumbnailUrl(boolean scaleToFit, int width, int height) {
    return getThumbnailUrl(scaleToFit, width, height, 100, "png");
  }

  public String getThumbnailUrl(boolean scaleToFit, int width, int height, int quality, String fmt) {
    if (AVOSCloud.getRegion() != AVOSCloud.REGION.NorthChina) {
      throw new UnsupportedOperationException("We only support this method for qiniu storage.");
    }
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Invalid width or height.");
    }
    if (quality < 1 || quality > 100) {
      throw new IllegalArgumentException("Invalid quality,valid range is 0-100.");
    }
    if (fmt == null || StringUtil.isEmpty(fmt.trim())) {
      fmt = "png";
    }
    int mode = scaleToFit ? 2 : 1;
    String resultUrl =
            this.getUrl() + String.format(THUMBNAIL_FMT, mode, width, height, quality, fmt);
    return resultUrl;
  }

  public AVACL getACL() {
    return acl;
  }

  public void setACL(AVACL acl) {
    this.acl = acl;
  }

  public synchronized void saveInBackground(final ProgressCallback progressCallback) {
    if (StringUtil.isEmpty(objectId)) {
      Uploader uploader = getUploader(progressCallback);
      uploader.execute();
    } else {
      if (null != progressCallback) {
        progressCallback.internalDone(100, null);
      }
    }
  }

  @Override
  public Observable<AVObject> saveInBackground() {
    JSONObject paramData = generateChangedParam();
    if (StringUtil.isEmpty(getObjectId())) {
      PaasClient.getStorageClient().newUploadToken(paramData.toJSONString())
              .subscribe(new Consumer<FileUploadToken>() {
                public void accept(FileUploadToken fileUploadToken) throws Exception {
                  AVFile.this.setObjectId(fileUploadToken.getObjectId());
                  AVFile.this.internalPutDirectly(KEY_URL, fileUploadToken.getUrl());
                  AVFile.this.internalPutDirectly(KEY_OBJECT_ID, fileUploadToken.getObjectId());
                  AVFile.this.internalPutDirectly(KEY_BUCKET, fileUploadToken.getBucket());
                  AVFile.this.internalPutDirectly(KEY_PROVIDER, fileUploadToken.getProvider());
                  Uploader uploader = getUploader(null);
                  AVException exception = uploader.execute();
                  if (null != exception) {

                  }
                }
              }, new Consumer<Throwable>() {
                public void accept(Throwable throwable) throws Exception {
                }
              });
      return null;
    } else {
      return Observable.just((AVObject) this);
    }
  }

  private Uploader getUploader(ProgressCallback progressCallback) {

    if (StringUtil.isEmpty(getUrl())) {
      return new FileUploader(this, progressCallback);
    } else {
      return new UrlDirectlyUploader(this, progressCallback);
    }
  }

  @JSONField(serialize = false)
  public byte[] getData() throws AVException {
    // FIXME: need to push background.
    String filePath = "";
    if(!StringUtil.isEmpty(localPath)) {
      filePath = localPath;
    } else if (!StringUtil.isEmpty(cachePath)) {
      filePath = cachePath;
    } else if (!StringUtil.isEmpty(getUrl())) {
      File cacheFile = FileCache.getIntance().getCacheFile(getUrl());
      if (null == cacheFile || !cacheFile.exists()) {
        FileDownloader downloader = new FileDownloader();
        downloader.execute(getUrl());
      }
      filePath = cacheFile.getAbsolutePath();
    }
    if(!StringUtil.isEmpty(filePath)) {
      return PersistenceUtil.sharedInstance().readContentBytesFromFile(new File(filePath));
    }
    return null;
  }

  @JSONField(serialize = false)
  public InputStream getDataStream() throws AVException {
    // FIXME: need to push background.
    String filePath = "";
    if(!StringUtil.isEmpty(localPath)) {
      filePath = localPath;
    } else if (!StringUtil.isEmpty(cachePath)) {
      filePath = cachePath;
    } else if (!StringUtil.isEmpty(getUrl())) {
      File cacheFile = FileCache.getIntance().getCacheFile(getUrl());
      if (null == cacheFile || !cacheFile.exists()) {
        FileDownloader downloader = new FileDownloader();
        downloader.execute(getUrl());
      }
      filePath = cacheFile.getAbsolutePath();
    }
    if(!StringUtil.isEmpty(filePath)) {
      return FileCache.getIntance().getInputStreamFromFile(new File(filePath));
    }
    return null;
  }
}
