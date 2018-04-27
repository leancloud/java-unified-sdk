package cn.leancloud.upload;

import cn.leancloud.*;
import cn.leancloud.AVFile;
import cn.leancloud.callback.ProgressCallback;
import cn.leancloud.core.PaasClient;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class FileUploader extends HttpClientUploader {
  private static AVLogger logger = LogUtil.getLogger(FileUploader.class);

  static final int gProgressGotToken = 10;
  static final int gProgressUploadedFile = 90;
  static final int gProgressComplete = 100;
  static final String gProviderQCloud = "qcloud";
  static final String gProviderS3 = "s3";

  private String token;
  private String bucket;
  private String uploadUrl;
  private String provider;

  public FileUploader(AVFile avFile, FileUploadToken uploadToken, ProgressCallback progressCallback) {
    super(avFile, progressCallback);
    this.token = uploadToken.getToken();
    this.bucket = uploadToken.getBucket();
    this.uploadUrl = uploadToken.getUploadUrl();
    this.provider = uploadToken.getProvider();
  }

  public AVException execute() {
    publishProgress(gProgressGotToken);
    Uploader uploader = getUploaderImplementation();
    if (null == uploader) {
      return new AVException(new Throwable("Uploader can not be instantiated."));
    }

    AVException uploadException = uploader.execute();
    if (uploadException == null) {
      publishProgress(gProgressComplete);
      completeFileUpload(true);
      return null;
    } else {
      completeFileUpload(false);
      return uploadException;
    }
  }

  private Uploader getUploaderImplementation() {
    if (!StringUtil.isEmpty(provider)) {
      if (gProviderQCloud.equalsIgnoreCase(provider)) {
        return new QCloudUploader(avFile, token, uploadUrl, progressCallback);
      } else if (gProviderS3.equalsIgnoreCase(provider)) {
        return new S3Uploader(avFile, uploadUrl, progressCallback);
      } else {
        return new QiniuSlicingUploader(avFile, token, progressCallback);
      }
    } else {
      logger.w("provider doesnot exist, cannot upload any file.");
      return null;
    }
  }

  private void completeFileUpload(boolean success){
    if (!StringUtil.isEmpty(token)) {
      try {
        JSONObject completeResult = new JSONObject();
        completeResult.put("result",success);
        completeResult.put("token",this.token);
        PaasClient.getStorageClient().fileCallback (completeResult);
      } catch (Exception e) {
        // ignore
      }
    }
  }

  protected static class ProgressCalculator {
    Map<Integer, Integer> blockProgress = new HashMap<Integer, Integer>();
    FileUploadProgressCallback callback;
    int fileBlockCount = 0;

    public ProgressCalculator(int blockCount, FileUploadProgressCallback callback) {
      this.callback = callback;
      this.fileBlockCount = blockCount;
    }

    public synchronized void publishProgress(int offset, int progress) {
      blockProgress.put(offset, progress);
      if (callback != null) {
        int progressSum = 0;
        Set<Integer> keySet = blockProgress.keySet();
        for (Integer index: keySet) {
          progressSum += blockProgress.get(index);
        }
        callback.onProgress(gProgressGotToken + (gProgressUploadedFile - gProgressGotToken)
                * progressSum / (100 * fileBlockCount));
      }
    }
  }

  public static void setUploadHeader(String key, String value) {
    UPLOAD_HEADERS.put(key, value);
  }

  public static interface FileUploadProgressCallback {
    void onProgress(int progress);
  }
  static HashMap<String, String> UPLOAD_HEADERS = new HashMap<String, String>();
}
