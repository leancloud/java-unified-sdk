package cn.leancloud;

import cn.leancloud.annotation.LCClassName;
import cn.leancloud.callback.ProgressCallback;
import cn.leancloud.codec.MDFive;
import cn.leancloud.core.LeanCloud;
import cn.leancloud.cache.FileCache;
import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.core.StorageClient;
import cn.leancloud.core.PaasClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import cn.leancloud.upload.*;
import cn.leancloud.utils.LCUtils;
import cn.leancloud.utils.FileUtil;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

@LCClassName("_File")
public final class LCFile extends LCObject {
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

  private transient String localPath = ""; // local file used by AVFile(name, file) constructor.

  private transient String cachePath = ""; // file cache path

  /**
   * default constructor.
   */
  public LCFile() {
    super(CLASS_NAME);
    if (AppConfiguration.getDefaultACL() != null) {
      this.acl = new LCACL(AppConfiguration.getDefaultACL());
    }
  }

  /**
   * constructor with name and data.
   * @param name file name.
   * @param data binary data.
   */
  public LCFile(String name, byte[] data) {
    this();
    if (null == data) {
      logger.w("data is illegal(null)");
      throw new IllegalArgumentException("data is illegal(null)");
    }
    internalPut(KEY_FILE_NAME, name);
    addMetaData(FILE_NAME_KEY, name);
    String md5 = MDFive.computeMD5(data);
    localPath = FileCache.getIntance().saveData(md5, data);
    addMetaData(FILE_SUM_KEY, md5);
    addMetaData(FILE_LENGTH_KEY, data.length);
    internalPut(KEY_MIME_TYPE, FileUtil.getMimeTypeFromFilename(name));
  }

  /**
   * constructor with name and local file.
   * @param name file name.
   * @param localFile local file.
   */
  public LCFile(String name, File localFile) {
    this();
    if (null == localFile || !localFile.exists() || !localFile.isFile()) {
      logger.w("local file is illegal");
      throw new IllegalArgumentException("local file is illegal.");
    }
    internalPut(KEY_FILE_NAME, name);
    addMetaData(FILE_NAME_KEY, name);
    String md5 = MDFive.computeFileMD5(localFile);
    localPath = localFile.getAbsolutePath();
    addMetaData(FILE_SUM_KEY, md5);
    addMetaData(FILE_LENGTH_KEY, localFile.length());
    internalPut(KEY_MIME_TYPE, FileUtil.getMimeTypeFromPath(localPath));
  }

  /**
   * constructor with name and external url.
   * @param name file name.
   * @param url external url.
   */
  public LCFile(String name, String url) {
    this(name, url, null);
  }

  /**
   * constructor with name and external url.
   * @param name file name
   * @param url external url.
   * @param metaData additional attributes.
   */
  public LCFile(String name, String url, Map<String, Object> metaData) {
    this(name, url, metaData, true);
  }

  protected LCFile(String name, String url, Map<String, Object> metaData, boolean external) {
    this();
    internalPut(KEY_FILE_NAME, name);
    addMetaData(FILE_NAME_KEY, name);
    internalPut(KEY_URL, url);
    Map<String, Object> meta = new HashMap<String, Object>();
    if (null != metaData) {
      LCUtils.putAllWithNullFilter(meta, metaData);
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

  private void internalPutDirectly(String key, Object value) {
    this.serverData.put(key, value);
  }

  /**
   * Get AVFile instance from objectId.
   * @param objectId file objectId.
   * @return observable instance.
   */
  public static Observable<LCFile> withObjectIdInBackground(final String objectId) {
    return PaasClient.getStorageClient().fetchFile(null, objectId);
  }

  /**
   * Get file name.
   * @return file name.
   */
  public String getName() {
    return (String) internalGet(KEY_FILE_NAME);
  }

  /**
   * Set file name.
   * @param name file name.
   */
  public void setName(String name) {
    internalPut(KEY_FILE_NAME, name);
  }

  /**
   * Get file meta data.
   * @return meta data.
   */
  public Map<String, Object> getMetaData() {
    Map<String, Object> result = (Map<String, Object>)internalGet(KEY_METADATA);
    if (null == result) {
      result = new HashMap<String, Object>();
      internalPut(KEY_METADATA, result);
    }
    return result;
  }

  /**
   * Set file meta data.
   * @param metaData meta data.
   */
  public void setMetaData(Map<String, Object> metaData) {
    internalPut(KEY_METADATA, metaData);
  }

  /**
   * Add new meta data.
   * @param key meta key.
   * @param val meta value.
   */
  public void addMetaData(String key, Object val) {
    Map<String, Object> metaData = getMetaData();
    metaData.put(key, val);
  }

  /**
   * Get file meta data.
   * @param key meta key.
   * @return meta value.
   */
  public Object getMetaData(String key) {
    return getMetaData().get(key);
  }

  /**
   * Remove file meta data.
   * @param key meta key.
   * @return old value.
   */
  public Object removeMetaData(String key) {
    return getMetaData().remove(key);
  }

  /**
   * Cleanup meta data.
   */
  public void clearMetaData() {
    getMetaData().clear();
  }

  /**
   * Get file size.
   * @return file size.
   */
  public int getSize() {
    Number size = (Number) getMetaData(FILE_LENGTH_KEY);
    if (size != null)
      return size.intValue();
    else
      return -1;
  }

  /**
   * Get file mime type.
   * @return mime type.
   */
  public String getMimeType() {
    return (String) internalGet(KEY_MIME_TYPE);
  }

  /**
   * Set file mime type.
   * @param mimeType mime type.
   */
  public void setMimeType(String mimeType) {
    internalPut(KEY_MIME_TYPE, mimeType);
  }

  /**
   * Get file key.
   * @return file key.
   */
  public String getKey() {
    return (String) internalGet(KEY_FILE_KEY);
  }

  /**
   * Set file key.
   * @param fileKey - fileKey
   * @notice this method needs authentication with masterKey!!
   *
   * File key is a part of url. After specified `fileKey`, the file's url should become `https://domain/fileKey`.
   * With the help of this method, developer can upload file with particular path. For example, upload a robots.txt file as following:
   *     File localFile = new File("./20160704174809.txt");
   *     AVFile file = new AVFile("testfilename", localFile);
   *     file.setKey("robots.txt");
   *     file.saveInBackground().blockingSubscribe();
   *
   */
  void setKey(String fileKey) {
    internalPut(KEY_FILE_KEY, fileKey);
  }

  /**
   * Get file bucket.
   * @return file bucket.
   */
  public String getBucket() {
    return (String) internalGet(KEY_BUCKET);
  }

  /**
   * Get file url.
   * @return file url.
   */
  public String getUrl() {
    return (String) internalGet(KEY_URL);
  }

  /**
   * Get file provider.
   * @return file provider.
   */
  public String getProvider() {
    return (String) internalGet(KEY_PROVIDER);
  }

  /**
   * Set file attribute.
   * @param key attribute key.
   * @param value attribute value.
   * notice: UnsupportedOperationException
   */
  @Override
  public void put(String key, Object value) {
    throw new UnsupportedOperationException("cannot invoke put method in AVFile");
  }

  /**
   * Get file attribute.
   * @param key attribute key.
   * @return attribute value.
   * notice: UnsupportedOperationException
   */
  @Override
  public Object get(String key) {
    throw new UnsupportedOperationException("cannot invoke get method in AVFile");
  }

  /**
   * Remove file attribute.
   * @param key attribute key.
   * notice: UnsupportedOperationException
   */
  @Override
  public void remove(String key) {
    throw new UnsupportedOperationException("cannot invoke remove method in AVFile");
  }

  /**
   * Increment file attribute.
   * @param key attribute key.
   * notice: UnsupportedOperationException
   */
  @Override
  public void increment(String key) {
    throw new UnsupportedOperationException("cannot invoke increment method in AVFile");
  }

  /**
   * Increment file attribute.
   * @param key attribute key.
   * @param value step value.
   * notice: UnsupportedOperationException
   */
  @Override
  public void increment(String key, Number value) {
    throw new UnsupportedOperationException("cannot invoke increment(Number) method in AVFile");
  }

  /**
   * Returns a thumbnail image url using QiNiu endpoints.
   *
   * @param scaleToFit scale param.
   * @param width width.
   * @param height height.
   * @return new url for thumbnail.
   * @see #getThumbnailUrl(boolean, int, int, int, String)
   */
  public String getThumbnailUrl(boolean scaleToFit, int width, int height) {
    return getThumbnailUrl(scaleToFit, width, height, 100, "png");
  }

  /**
   * Returns a thumbnail image url using QiNiu endpoints.
   * @param scaleToFit scale param.
   * @param width width
   * @param height height
   * @param quality quality.
   * @param fmt format string.
   * @return new url for thumbnail.
   */
  public String getThumbnailUrl(boolean scaleToFit, int width, int height, int quality, String fmt) {
    if (LeanCloud.getRegion() == LeanCloud.REGION.NorthAmerica) {
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

  /**
   * Get map data of current file.
   * @return map data.
   */
  public Map<String, Object> toMap() {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("__type", CLASS_NAME);
    result.put(KEY_METADATA, getMetaData());

    if (!StringUtil.isEmpty(getUrl())) {
      result.put(KEY_URL, getUrl());
    }

    if (!StringUtil.isEmpty(getObjectId())) {
      result.put(LCObject.KEY_OBJECT_ID, getObjectId());
    }

    result.put("id", getName());

    return result;
  }

  /**
   * save to cloud backend.
   * @param keepFileName whether keep file name in url or not.
   * @param progressCallback progress callback.
   */
  public synchronized void saveInBackground(boolean keepFileName, final ProgressCallback progressCallback) {
    saveInBackground(null, keepFileName, progressCallback);
  }

  /**
   * save to cloud in background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param keepFileName whether keep file name in url or not.
   * @param progressCallback progress callback.
   *
   * in general, this method should be invoked in lean engine.
   */
  public synchronized void saveInBackground(LCUser asAuthenticatedUser,
                                            boolean keepFileName, final ProgressCallback progressCallback) {
    saveWithProgressCallback(asAuthenticatedUser, keepFileName, progressCallback).subscribe(new Observer<LCFile>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCFile avFile) {
        if (null != progressCallback) {
          progressCallback.internalDone(100, null);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        if (null != progressCallback) {
          progressCallback.internalDone(90, new LCException(throwable));
        }
      }

      @Override
      public void onComplete() {

      }
    });
  }

  /**
   * save to cloud backend.
   * @param progressCallback progress callback.
   */
  public void saveInBackground(final ProgressCallback progressCallback) {
    saveInBackground(false, progressCallback);
  }

  private Observable<LCFile> directlyCreate(LCUser asAuthenticatedUser, final JSONObject parameters) {
    return PaasClient.getStorageClient().createObject(asAuthenticatedUser,
            this.className, parameters, false, null)
            .map(new Function<LCObject, LCFile>() {
              @Override
              public LCFile apply(LCObject LCObject) throws Exception {
                LCUtils.putAllWithNullFilter(LCFile.this.serverData, parameters);
                LCFile.this.mergeRawData(LCObject, true);
                LCFile.this.onSaveSuccess();
                return LCFile.this;
              }});
  }

  private boolean isSavingExternalFile() {
    return StringUtil.isEmpty(getObjectId()) && !StringUtil.isEmpty(getUrl());
  }

  private Observable<LCFile> saveWithProgressCallback(final LCUser asAuthenticatedUser,
                                                      boolean keepFileName, final ProgressCallback callback) {
    JSONObject paramData = generateChangedParam();
//    final String fileKey = FileUtil.generateFileKey(this.getName(), keepFileName);
//    paramData.put("key", fileKey);
    paramData.put("__type", "File");
    if (StringUtil.isEmpty(getObjectId())) {
      if (!StringUtil.isEmpty(getUrl())) {
        return directlyCreate(asAuthenticatedUser, paramData);
      }
      logger.d("createToken params: " + paramData.toJSONString() + ", " + this);
      StorageClient storageClient = PaasClient.getStorageClient();
      Observable<LCFile> result = storageClient.newUploadToken(asAuthenticatedUser, paramData)
              .map(new Function<FileUploadToken, LCFile>() {
                public LCFile apply(@NonNull FileUploadToken fileUploadToken) throws Exception {
                  logger.d("[Thread:" + Thread.currentThread().getId() + "]" + fileUploadToken.toString() + ", " + LCFile.this);
                  LCFile.this.setObjectId(fileUploadToken.getObjectId());
                  LCFile.this.internalPutDirectly(KEY_OBJECT_ID, fileUploadToken.getObjectId());
                  LCFile.this.internalPutDirectly(KEY_BUCKET, fileUploadToken.getBucket());
                  LCFile.this.internalPutDirectly(KEY_PROVIDER, fileUploadToken.getProvider());
                  LCFile.this.internalPutDirectly(KEY_FILE_KEY, fileUploadToken.getKey());

                  Uploader uploader = new FileUploader(LCFile.this, fileUploadToken, callback);
                  LCFile.this.internalPutDirectly(KEY_URL, fileUploadToken.getUrl());

                  LCException exception = uploader.execute();

                  JSONObject completeResult = JSONObject.Builder.create(null);
                  completeResult.put("result", null == exception);
                  completeResult.put("token",fileUploadToken.getToken());
                  logger.d("file upload result: " + completeResult.toJSONString());
                  try {
                    PaasClient.getStorageClient().fileCallback(asAuthenticatedUser, completeResult);
                    if (null != exception) {
                      logger.w("failed to invoke fileCallback. cause:", exception);
                      throw exception;
                    } else {
                      return LCFile.this;
                    }
                  } catch (IOException ex) {
                    logger.w(ex);
                    throw ex;
                  }
                }
              });
      result = storageClient.wrapObservable(result);
      return result;
    } else {
      logger.d("file has been upload to cloud, ignore update request.");
      return Observable.just(this);
    }
  }

  /**
   * save to cloud backend.
   * @return  Observable object.
   */
  @Override
  public Observable<LCFile> saveInBackground() {
    return saveInBackground(false);
  }

  /**
   * save to cloud.
   */
  @Override
  public void save() {
    this.saveInBackground().blockingSubscribe();
  }

  /**
   * save to cloud
   * @param asAuthenticatedUser explicit user for request authentication.
   *
   */
  public void save(LCUser asAuthenticatedUser) {
    this.saveInBackground(asAuthenticatedUser, false).blockingSubscribe();
  }

  /**
   * save to cloud backend.
   * @param keepFileName whether keep file name in url or not.
   * @return Observable object.
   */
  public Observable<LCFile> saveInBackground(boolean keepFileName) {
    return saveInBackground(null, keepFileName);
  }

  /**
   * Save eventually(not supported).
   * @throws LCException exception happened.
   *
   * Because that file size is too big, cache the entire data will cost much disk capacity,
   * so we don't support this method at present.
   */
  public void saveEventually() throws LCException {
    saveEventually(null);
  }

  /**
   * Save eventually(not supported).
   * @param asAuthenticatedUser explicit user for request authentication.
   * @throws LCException exception happened.
   *
   * Because that file size is too big, cache the entire data will cost much disk capacity,
   * so we don't support this method at present.
   */
  public void saveEventually(final LCUser asAuthenticatedUser) throws LCException {
    if (isSavingExternalFile()) {
      super.saveEventually(asAuthenticatedUser);
    } else {
      throw new UnsupportedOperationException("AVFile#saveEventually is not allowed," +
              " please save the binary data to temp store and try to save in future.");
    }
  }
  /**
   * save to cloud in background.
   * @param asAuthenticatedUser explicit user for request authentication.
   * @param keepFileName whether keep file name in url or not.
   * @return Observable object.
   *
   * in general, this method should be invoked in lean engine.
   */
  public Observable<LCFile> saveInBackground(LCUser asAuthenticatedUser, boolean keepFileName) {
    return saveWithProgressCallback(asAuthenticatedUser, keepFileName,null);
  }

  /**
   * Get data in blocking mode.
   * @return data bytes.
   */
  //@JSONField(serialize = false)
  public byte[] getData() {
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

  /**
   * Get data in async mode.
   * @return observable instance.
   */
  public Observable<byte[]> getDataInBackground() {
    Observable observable = Observable.fromCallable(new Callable<byte[]>() {
      @Override
      public byte[] call() throws Exception {
        return getData();
      }
    });
    if (AppConfiguration.isAsynchronized()) {
      observable = observable.subscribeOn(Schedulers.io());
    }
    AppConfiguration.SchedulerCreator defaultScheduler = AppConfiguration.getDefaultScheduler();
    if (null != defaultScheduler) {
      observable = observable.observeOn(defaultScheduler.create());
    }
    return observable;
  }

  /**
   * Get data stream in blocking mode.
   * @return data stream.
   * @throws Exception for file not found or io problem.
   */
  public InputStream getDataStream() throws Exception {
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

  /**
   * Get data stream in async mode.
   * @return observable instance.
   */
  public Observable<InputStream> getDataStreamInBackground() {
    Observable<InputStream> observable = Observable.fromCallable(new Callable<InputStream>() {
      @Override
      public InputStream call() throws Exception {
        return getDataStream();
      }
    });
    if (AppConfiguration.isAsynchronized()) {
      observable = observable.subscribeOn(Schedulers.io());
    }
    AppConfiguration.SchedulerCreator defaultScheduler = AppConfiguration.getDefaultScheduler();
    if (null != defaultScheduler) {
      observable = observable.observeOn(defaultScheduler.create());
    }
    return observable;
  }

  /**
   * Generate File instance with local path.
   * @param name file name
   * @param absoluteLocalFilePath local path.
   * @return file instance.
   * @throws FileNotFoundException file not found.
   */
  public static LCFile withAbsoluteLocalPath(String name, String absoluteLocalFilePath)
          throws FileNotFoundException {
    return withFile(name, new File(absoluteLocalFilePath));
  }

  /**
   * Generate File instance with local file.
   * @param name file name.
   * @param file local file.
   * @return file instance.
   * @throws FileNotFoundException file not found.
   */
  public static LCFile withFile(String name, File file) throws FileNotFoundException {
    if (file == null) {
      throw new IllegalArgumentException("null file object.");
    }
    if (!file.exists() || !file.isFile()) {
      throw new FileNotFoundException();
    }
    LCFile avFile = new LCFile(name, file);
    LCUser currentUser = LCUser.getCurrentUser();
    if (null != currentUser && !StringUtil.isEmpty(currentUser.getObjectId())) {
      avFile.addMetaData("owner", currentUser.getObjectId());
    }
    return avFile;
  }
}
