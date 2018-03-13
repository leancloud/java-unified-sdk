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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import cn.leancloud.upload.*;
import cn.leancloud.AVLogger;
import cn.leancloud.utils.FileUtil;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public final class AVFile extends AVObject {
  private static AVLogger LOGGER = LogUtil.getLogger(AVFile.class);
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

  @JSONField(serialize = false)
  private String localPath = ""; // local file used by AVFile(name, file) constructor.

  @JSONField(serialize = false)
  private String cachePath = ""; // file cache path

  public AVFile() {
    super(CLASS_NAME);
    if (PaasClient.getDefaultACL() != null) {
      this.acl = new AVACL(PaasClient.getDefaultACL());
    }
  }

  public AVFile(String name, byte[] data) {
    this();
    if (null == data) {
      LOGGER.w("data is illegal(null)");
      throw new IllegalArgumentException("data is illegal(null)");
    }
    internalPut(KEY_FILE_NAME, name);
    addMetaData(FILE_NAME_KEY, name);
    String md5 = MD5.computeMD5(data);
    localPath = FileCache.getIntance().saveData(md5, data);
    addMetaData(FILE_SUM_KEY, md5);
    addMetaData(FILE_LENGTH_KEY, data.length);
    internalPut(KEY_MIME_TYPE, FileUtil.getMimeTypeFromLocalFile(name));
    LOGGER.d("localpath=" + localPath);
  }

  public AVFile(String name, File localFile) {
    this();
    if (null == localFile || !localFile.exists() || !localFile.isFile()) {
      LOGGER.w("local file is illegal");
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
    Map<String, Object> result = (Map<String, Object>)internalGet(KEY_METADATA);
    if (null == result) {
      result = new HashMap<String, Object>();
      internalPut(KEY_METADATA, result);
    }
    return result;
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

  public Object removeMetaData(String key) {
    return getMetaData().remove(key);
  }

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

  public String getKey() {
    return (String) internalGet(KEY_FILE_KEY);
  }

  public String getBucket() {
    return (String) internalGet(KEY_BUCKET);
  }

  public String getUrl() {
    return (String) internalGet(KEY_URL);
  }

  public String getProvider() {
    return (String) internalGet(KEY_PROVIDER);
  }

  public AVACL getACL() {
    return acl;
  }

  public void setACL(AVACL acl) {
    this.acl = acl;
  }

  @Override
  public void put(String key, Object value) {
    LOGGER.w("cannot invoke put method in AVFile");
    throw new UnsupportedOperationException("cannot invoke put method in AVFile");
  }

  @Override
  public Object get(String key) {
    LOGGER.w("cannot invoke get method in AVFile");
    throw new UnsupportedOperationException("cannot invoke get method in AVFile");
  }

  @Override
  public void remove(String key) {
    LOGGER.w("cannot invoke remove method in AVFile");
    throw new UnsupportedOperationException("cannot invoke remove method in AVFile");
  }

  @Override
  public void increment(String key) {
    LOGGER.w("cannot invoke increment method in AVFile");
    throw new UnsupportedOperationException("cannot invoke increment method in AVFile");
  }

  @Override
  public void increment(String key, Number value) {
    LOGGER.w("cannot invoke increment method in AVFile");
    throw new UnsupportedOperationException("cannot invoke increment method in AVFile");
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
      LOGGER.w("We only support this method for qiniu storage.");
      throw new UnsupportedOperationException("We only support this method for qiniu storage.");
    }
    if (width < 0 || height < 0) {
      LOGGER.w("Invalid width or height.");
      throw new IllegalArgumentException("Invalid width or height.");
    }
    if (quality < 1 || quality > 100) {
      LOGGER.w("Invalid quality,valid range is 0-100.");
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

  public synchronized void saveInBackground(final ProgressCallback progressCallback) {
    if (StringUtil.isEmpty(objectId)) {
      Uploader uploader = getUploader(null, progressCallback);
      uploader.execute();
    } else {
      if (null != progressCallback) {
        progressCallback.internalDone(100, null);
      }
    }
  }

  @Override
  public Observable<AVFile> saveInBackground() {
    JSONObject paramData = generateChangedParam();
    final String fileKey = FileUtil.generateFileKey(this.getName());
    paramData.put("key", fileKey);
    paramData.put("__type", "File");
    if (StringUtil.isEmpty(getObjectId())) {
      LOGGER.d("createToken params: " + paramData.toJSONString() + ", " + this);
      return PaasClient.getStorageClient().newUploadToken(paramData)
              .map(new Function<FileUploadToken, AVFile>() {
                public AVFile apply(@NonNull FileUploadToken fileUploadToken) throws Exception {
                  LOGGER.d(fileUploadToken.toString() + ", " + AVFile.this);
                  AVFile.this.setObjectId(fileUploadToken.getObjectId());
                  AVFile.this.internalPutDirectly(KEY_OBJECT_ID, fileUploadToken.getObjectId());
                  AVFile.this.internalPutDirectly(KEY_BUCKET, fileUploadToken.getBucket());
                  AVFile.this.internalPutDirectly(KEY_PROVIDER, fileUploadToken.getProvider());
                  AVFile.this.internalPutDirectly(KEY_FILE_KEY, fileKey);

                  Uploader uploader = AVFile.this.getUploader(fileUploadToken, null);
                  AVFile.this.internalPutDirectly(KEY_URL, fileUploadToken.getUrl());

                  AVException exception = uploader.execute();

                  JSONObject completeResult = new JSONObject();
                  completeResult.put("result", null == exception);
                  completeResult.put("token",fileUploadToken.getToken());
                  LOGGER.d("file upload result: " + completeResult.toJSONString());
                  try {
                    PaasClient.getStorageClient().fileCallback(completeResult);
                    if (null != exception) {
                      LOGGER.w("failed to invoke fileCallback. cause:", exception);
                      throw exception;
                    } else {
                      return AVFile.this;
                    }
                  } catch (IOException ex) {
                    LOGGER.w(ex);
                    throw ex;
                  }
                }
              });
    } else {
      LOGGER.d("file has been upload to cloud, ignore request.");
      return Observable.just((AVFile) this);
    }
  }

  @Override
  public Observable<Void> deleteInBackground() {
    return super.deleteInBackground();
  }

  private Uploader getUploader(FileUploadToken uploadToken, ProgressCallback progressCallback) {

    if (StringUtil.isEmpty(getUrl())) {
      return new FileUploader(this, uploadToken, progressCallback);
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
        downloader.execute(getUrl(), cacheFile);
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
        downloader.execute(getUrl(), cacheFile);
      }
      filePath = cacheFile.getAbsolutePath();
    }
    if(!StringUtil.isEmpty(filePath)) {
      LOGGER.d("dest file path=" + filePath);
      return FileCache.getIntance().getInputStreamFromFile(new File(filePath));
    }
    return null;
  }
}
