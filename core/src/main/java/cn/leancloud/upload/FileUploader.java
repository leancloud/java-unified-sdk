package cn.leancloud.upload;

import cn.leancloud.AVCallback;
import cn.leancloud.AVException;
import cn.leancloud.ProgressCallback;
import cn.leancloud.SaveCallback;
import cn.leancloud.core.AVFile;
import cn.leancloud.core.ops.ObjectFieldOperation;
import cn.leancloud.core.ops.OperationBuilder;
import cn.leancloud.network.PaasClient;
import cn.leancloud.utils.AVUtils;
import cn.leancloud.utils.FileUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class FileUploader extends HttpClientUploader {
  static final int PROGRESS_GET_TOKEN = 10;
  static final int PROGRESS_UPLOAD_FILE = 90;
  static final int PROGRESS_COMPLETE = 100;
  static final String PROVIDER_QCLOUD = "qcloud";
  static final String PROVIDER_S3 = "s3";

  private String token;
  private String bucket;
  private String uploadUrl;
  private String provider;

  public FileUploader(AVFile avFile, ProgressCallback progressCallback) {
    super(avFile, progressCallback);
  }

  public AVException execute() {
    publishProgress(PROGRESS_GET_TOKEN);
    Uploader uploader = getUploaderImplementation();
    if (null == uploader) {
      return new AVException(new Throwable("Uploader can not be instantiated."));
    }

    AVException uploadException = uploader.execute();
    if (uploadException == null) {
      publishProgress(PROGRESS_COMPLETE);
      completeFileUpload(true);
      return null;
    } else {
      completeFileUpload(false);
      return uploadException;
    }
  }

  private Uploader getUploaderImplementation() {
    if (!StringUtil.isEmpty(provider)) {
      if (PROVIDER_QCLOUD.equalsIgnoreCase(provider)) {
        return new QCloudUploader(avFile, token, uploadUrl, progressCallback);
      } else if (PROVIDER_S3.equalsIgnoreCase(provider)) {
        return new S3Uploader(avFile, uploadUrl, progressCallback);
      } else {
        return new QiniuSlicingUploader(avFile, token, progressCallback);
      }
    } else {
      return null;
    }
  }

  private void completeFileUpload(boolean success){
    if (!StringUtil.isEmpty(token)) {
      try {
        JSONObject completeResult = new JSONObject();
        completeResult.put("result",success);
        completeResult.put("token",this.token);
        PaasClient.getStorageClient().fileCallback (completeResult.toJSONString());
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
        callback.onProgress(PROGRESS_GET_TOKEN + (PROGRESS_UPLOAD_FILE - PROGRESS_GET_TOKEN)
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
