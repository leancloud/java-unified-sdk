package cn.leancloud.core;

import cn.leancloud.network.PaasClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.annotation.JSONField;
import io.reactivex.Observable;

public final class AVFile {
  private static long MAX_FILE_BUF_SIZE = 1024 * 2014 * 4;
  public static final String DEFAULTMIMETYPE = "application/octet-stream";
  private static final String FILE_SUM_KEY = "_checksum";
  private static final String FILE_NAME_KEY = "_name";
  private static final String FILE_SOURCE_KEY = "__source";
  private static final String FILE_SOURCE_EXTERNAL = "external";
  private static final String ELDERMETADATAKEYFORIOSFIX = "metadata";
  private static final String THUMBNAIL_FMT = "?imageView/%d/w/%d/h/%d/q/%d/format/%s";
  public static final String AVFILE_ENDPOINT = "files";

  private String name = "";
  private Map<String, Object> metaData = new ConcurrentHashMap<String, Object>();
  private String url = null;
  private String localPath = null;
  private String objectId = null;
  private String updatedAt = null;
  private String createdAt = null;
  private String bucket = null;
  private AVACL acl = null;


  public AVFile() {
    if (PaasClient.getDefaultACL() != null) {
      this.acl = new AVACL(PaasClient.getDefaultACL());
    }
  }

  public AVFile(String name, byte[] data) {
  }

  public AVFile(String name, String url) {
    this(name, url, null);
  }

  public AVFile(String name, String url, Map<String, Object> metaData) {
    this(name, url, metaData, true);
  }

  protected AVFile(String name, String url, Map<String, Object> metaData, boolean external) {
    this();
    this.name = name;
    this.url = url;
    if (metaData != null) {
      this.metaData.putAll(metaData);
    }
    if (external) {
      this.metaData.put(FILE_SOURCE_KEY, FILE_SOURCE_EXTERNAL);
    }
  }

  public static Observable<AVFile> withObjectIdInBackground(final String objectId) {
    return null;
  }

  public static String getClassName() {
    return "_File";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getMetaData() {
    return metaData;
  }

  public void setMetaData(Map<String, Object> metaData) {
    this.metaData = metaData;
  }

  public Object addMetaData(String key, Object val) {
    return metaData.put(key, val);
  }

  public Object getMetaData(String key) {
    return this.metaData.get(key);
  }

  /**
   * Remove file meta data.
   *
   * @param key The meta data's key
   * @return The metadata value.
   * @since 1.3.4
   */
  public Object removeMetaData(String key) {
    return metaData.remove(key);
  }

  /**
   * Clear file metadata.
   */
  public void clearMetaData() {
    this.metaData.clear();
  }

  public int getSize() {
    Number size = (Number) getMetaData("size");
    if (size != null)
      return size.intValue();
    else
      return -1;
  }

  public static String getMimeType(String url) {
    String type = DEFAULTMIMETYPE;
//    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
//    if (extension != null) {
//      MimeTypeMap mime = MimeTypeMap.getSingleton();
//      type = mime.getMimeTypeFromExtension(extension);
//    }
//    if (type == null) {
//      type = DEFAULTMIMETYPE;
//    }
    return type;
  }

  public String getBucket() {
    return this.bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getUrl() {
    return url;
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
    return this.getThumbnailUrl(scaleToFit, width, height, 100, "png");
  }

  public String getThumbnailUrl(boolean scaleToFit, int width, int height, int quality, String fmt) {
    if (!AVOSCloud.isCN() || AppRouterManager.isQCloudApp(AVOSCloud.applicationId)) {
      throw new UnsupportedOperationException("We only support this method for qiniu storage.");
    }
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Invalid width or height.");
    }
    if (quality < 1 || quality > 100) {
      throw new IllegalArgumentException("Invalid quality,valid range is 0-100.");
    }
    if (fmt == null || AVUtils.isBlankString(fmt.trim())) {
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

  public synchronized void saveInBackground(final SaveCallback saveCallback,
                                            final ProgressCallback progressCallback) {
    if (AVUtils.isBlankString(objectId)) {
      cancelUploadIfNeed();
      uploader = getUploader(saveCallback, progressCallback);
      uploader.execute();
    } else {
      if (null != saveCallback) {
        saveCallback.internalDone(null);
      }
      if (null != progressCallback) {
        progressCallback.internalDone(100, null);
      }
    }
  }

  /**
   * Saves the file to the AVOSCloud cloud in a background thread.
   *
   * @param callback A SaveCallback that gets called when the save completes.
   */
  public void saveInBackground(SaveCallback callback) {
    saveInBackground(callback, null);
  }

  /**
   * Saves the file to the AVOSCloud cloud in a background thread.
   */
  public void saveInBackground() {
    saveInBackground(null);
  }

  public void deleteInBackground() {
    ;
  }

  @JSONField(serialize = false)
  public InputStream getDataStream() throws AVException {
    String filePath = "";
    if(!AVUtils.isBlankString(localPath)) {
      filePath = localPath;
    } else if (!AVUtils.isBlankString(localTmpFilePath)) {
      filePath = localTmpFilePath;
    } else if (!AVUtils.isBlankString(url)) {
      File cacheFile = AVFileDownloader.getCacheFile(url);
      if (null == cacheFile || !cacheFile.exists()) {
        if (!AVUtils.isConnected(AVOSCloud.applicationContext)) {
          throw new AVException(AVException.CONNECTION_FAILED, "Connection lost");
        } else {
          cancelDownloadIfNeed();
          downloader = new AVFileDownloader();
          AVException exception = downloader.doWork(getUrl());
          if (exception != null) {
            throw exception;
          }
        }
      }
      filePath = cacheFile.getAbsolutePath();
    }
    if(!AVUtils.isBlankString(filePath)) {
      try {
        return AVPersistenceUtils.getInputStreamFromFile(new File(filePath));
      } catch (IOException e){
        throw new AVException(e);
      }
    }
    return null;
  }
}
