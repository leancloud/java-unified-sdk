package cn.leancloud;

import cn.leancloud.callback.ProgressCallback;
import cn.leancloud.codec.MD5;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.cache.FileCache;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.ops.ObjectFieldOperation;
import cn.leancloud.ops.OperationBuilder;
import cn.leancloud.core.PaasClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import cn.leancloud.upload.*;
import cn.leancloud.utils.FileUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

@JSONType(deserializer = ObjectTypeAdapter.class, serializer = ObjectTypeAdapter.class)
public final class AVFile extends AVObject {
  public static final String CLASS_NAME = "_File";

  private static final String FILE_SUM_KEY = "_checksum";
  private static final String FILE_NAME_KEY = "_name";
  private static final String FILE_LENGTH_KEY = "size";
  private static final String FILE_SOURCE_KEY = "__source";
  private static final String FILE_SOURCE_EXTERNAL = "external";

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
    if (AppConfiguration.getDefaultACL() != null) {
      this.acl = new AVACL(AppConfiguration.getDefaultACL());
    }
  }

  public AVFile(String name, byte[] data) {
    this();
    if (null == data) {
      logger.w("data is illegal(null)");
      throw new IllegalArgumentException("data is illegal(null)");
    }
    internalPut(KEY_FILE_NAME, name);
    addMetaData(FILE_NAME_KEY, name);
    String md5 = MD5.computeMD5(data);
    localPath = FileCache.getIntance().saveData(md5, data);
    addMetaData(FILE_SUM_KEY, md5);
    addMetaData(FILE_LENGTH_KEY, data.length);
    internalPut(KEY_MIME_TYPE, FileUtil.getMimeTypeFromLocalFile(name));
    logger.d("localpath=" + localPath);
  }

  public AVFile(String name, File localFile) {
    this();
    if (null == localFile || !localFile.exists() || !localFile.isFile()) {
      logger.w("local file is illegal");
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

  // add for avoiding sonarqube check, idiot sonar
  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  // add for avoiding sonarqube check, idiot sonar
  @Override
  public int hashCode() {
    return super.hashCode();
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
    ObjectFieldOperation op = OperationBuilder.gBuilder.create(OperationBuilder.OperationType.Set, key, value);
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
    throw new UnsupportedOperationException("cannot invoke remove method in AVFile");
  }

  @Override
  public void increment(String key) {
    throw new UnsupportedOperationException("cannot invoke increment method in AVFile");
  }

  @Override
  public void increment(String key, Number value) {
    throw new UnsupportedOperationException("cannot invoke increment(Number) method in AVFile");
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
      logger.w("We only support this method for qiniu storage.");
      throw new UnsupportedOperationException("We only support this method for qiniu storage.");
    }
    if (width < 0 || height < 0) {
      logger.w("Invalid width or height.");
      throw new IllegalArgumentException("Invalid width or height.");
    }
    if (quality < 1 || quality > 100) {
      logger.w("Invalid quality,valid range is 0-100.");
      throw new IllegalArgumentException("Invalid quality,valid range is 0-100.");
    }
    if (fmt == null || StringUtil.isEmpty(fmt.trim())) {
      fmt = "png";
    }
    int mode = scaleToFit ? 2 : 1;
    return this.getUrl() + String.format(THUMBNAIL_FMT, mode, width, height, quality, fmt);
  }

  public Map<String, Object> toMap() {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("__type", CLASS_NAME);
    result.put(KEY_METADATA, getMetaData());

    if (!StringUtil.isEmpty(getUrl())) {
      result.put(KEY_URL, getUrl());
    }

    if (!StringUtil.isEmpty(getObjectId())) {
      result.put(AVObject.KEY_OBJECT_ID, getObjectId());
    }

    result.put("id", getName());

    return result;
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
      logger.d("createToken params: " + paramData.toJSONString() + ", " + this);
      return PaasClient.getStorageClient().newUploadToken(paramData)
              .map(new Function<FileUploadToken, AVFile>() {
                public AVFile apply(@NonNull FileUploadToken fileUploadToken) throws Exception {
                  logger.d("[Thread:" + Thread.currentThread().getId() + "]" + fileUploadToken.toString() + ", " + AVFile.this);
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
                  logger.d("file upload result: " + completeResult.toJSONString());
                  try {
                    PaasClient.getStorageClient().fileCallback(completeResult);
                    if (null != exception) {
                      logger.w("failed to invoke fileCallback. cause:", exception);
                      throw exception;
                    } else {
                      return AVFile.this;
                    }
                  } catch (IOException ex) {
                    logger.w(ex);
                    throw ex;
                  }
                }
              });
    } else {
      logger.d("file has been upload to cloud, ignore request.");
      return Observable.just((AVFile) this);
    }
  }

  private Uploader getUploader(FileUploadToken uploadToken, ProgressCallback progressCallback) {

    if (StringUtil.isEmpty(getUrl())) {
      return new FileUploader(this, uploadToken, progressCallback);
    } else {
      return new UrlDirectlyUploader(this, progressCallback);
    }
  }

  @JSONField(serialize = false)
  public byte[] getData() {
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
      if (null != cacheFile) {
        filePath = cacheFile.getAbsolutePath();
      }
    }
    if(!StringUtil.isEmpty(filePath)) {
      return PersistenceUtil.sharedInstance().readContentBytesFromFile(new File(filePath));
    }
    return new byte[0];
  }

  @JSONField(serialize = false)
  public InputStream getDataStream() {
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
      if (null != cacheFile) {
        filePath = cacheFile.getAbsolutePath();
      }
    }
    if(!StringUtil.isEmpty(filePath)) {
      logger.d("dest file path=" + filePath);
      return FileCache.getIntance().getInputStreamFromFile(new File(filePath));
    }
    logger.w("failed to get dataStream.");
    return null;
  }

  public static AVFile withAbsoluteLocalPath(String name, String absoluteLocalFilePath)
          throws FileNotFoundException {
    return withFile(name, new File(absoluteLocalFilePath));
  }

  public static AVFile withFile(String name, File file) throws FileNotFoundException {
    if (file == null) {
      throw new IllegalArgumentException("null file object.");
    }
    if (!file.exists() || !file.isFile()) {
      throw new FileNotFoundException();
    }

    AVFile avFile = new AVFile();
    avFile.setName(name);
    long fileSize = file.length();
    String fileMD5 = "";
    try {
      InputStream is = PersistenceUtil.getInputStreamFromFile(file);
      MD5 md5 = MD5.getInstance();
      md5.prepare();
      if (null != is) {
        byte buf[] = new byte[(int)PersistenceUtil.MAX_FILE_BUF_SIZE];
        int len;
        while ((len = is.read(buf)) != -1) {
          md5.update(buf, 0, len);
        }
        byte[] md5bytes = md5.digest();
        fileMD5 = MD5.hexEncodeBytes(md5bytes);
        is.close();
      }
    } catch (Exception ex) {
      fileMD5 = "";
    }
    avFile.addMetaData("size", fileSize);
    avFile.addMetaData(FILE_SUM_KEY, fileMD5);

    AVUser currentUser = AVUser.getCurrentUser();
    avFile.addMetaData("owner", currentUser != null ? currentUser.getObjectId() : "");
    avFile.addMetaData(FILE_NAME_KEY, name);
    return avFile;
  }
}
