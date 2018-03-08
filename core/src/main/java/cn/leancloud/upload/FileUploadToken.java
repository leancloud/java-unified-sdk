package cn.leancloud.upload;

import cn.leancloud.utils.AVUtils;
import com.alibaba.fastjson.annotation.JSONField;

public class FileUploadToken {
  private String bucket = null;

  private String objectId = null;

  @JSONField(name = "upload_url")
  private String uploadUrl = null;

  private String provider = null;

  private String token = null;

  private String url = null;

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

  @java.lang.Override
  public java.lang.String toString() {
    return "FileUploadToken{" +
            "bucket='" + bucket + '\'' +
            ", objectId='" + objectId + '\'' +
            ", uploadUrl='" + uploadUrl + '\'' +
            ", provider='" + provider + '\'' +
            ", token='" + token + '\'' +
            ", url='" + url + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FileUploadToken that = (FileUploadToken) o;
    return AVUtils.equals(bucket, that.bucket) &&
            AVUtils.equals(objectId, that.objectId) &&
            AVUtils.equals(uploadUrl, that.uploadUrl) &&
            AVUtils.equals(provider, that.provider) &&
            AVUtils.equals(token, that.token) &&
            AVUtils.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return AVUtils.hash(bucket, objectId, uploadUrl, provider, token, url);
  }
}
