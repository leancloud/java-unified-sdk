package cn.leancloud.upload;

import cn.leancloud.utils.LCUtils;

public class FileUploadToken {
  private String bucket = null;

  private String objectId = null;

  private String uploadUrl = null;

  private String provider = null;

  private String token = null;

  private String url = null;

  private String key = null;

  public FileUploadToken() {
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getUploadUrl() {
    return uploadUrl;
  }

  public void setUploadUrl(String uploadUrl) {
    this.uploadUrl = uploadUrl;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public java.lang.String toString() {
    return "FileUploadToken{" +
            "bucket='" + bucket + '\'' +
            ", objectId='" + objectId + '\'' +
            ", uploadUrl='" + uploadUrl + '\'' +
            ", provider='" + provider + '\'' +
            ", token='" + token + '\'' +
            ", url='" + url + '\'' +
            ", key='" + key + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FileUploadToken that = (FileUploadToken) o;
    return LCUtils.equals(bucket, that.bucket) &&
            LCUtils.equals(objectId, that.objectId) &&
            LCUtils.equals(uploadUrl, that.uploadUrl) &&
            LCUtils.equals(provider, that.provider) &&
            LCUtils.equals(token, that.token) &&
            LCUtils.equals(url, that.url) &&
            LCUtils.equals(key, that.key);
  }

  @Override
  public int hashCode() {
    return LCUtils.hash(bucket, objectId, uploadUrl, provider, token, url, key);
  }
}
