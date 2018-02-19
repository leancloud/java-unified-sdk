package cn.leancloud.upload;

import cn.leancloud.AVException;
import cn.leancloud.ProgressCallback;
import cn.leancloud.SaveCallback;
import cn.leancloud.core.AVFile;
import cn.leancloud.core.cache.FileCache;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class QCloudUploader extends HttpClientUploader {
  private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
  private static final String FILE_CONTENT = "filecontent";
  private static final String PARAM_OP = "op";
  private static final String PARAM_SHA = "sha";
  private static final String MULTIPART_FORM_DATA = "multipart/form-data";
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String PARAM_FILE_SIZE = "filesize";
  private static final String PARAM_SLICE_SIZE = "slice_size";
  private static final String PARAM_OFFSET = "offset";
  private static final String PARAM_SESSION = "session";
  private static final String OP_UPLOAD_SLICE = "upload_slice";
  private static final String OP_UPLOAD = "upload";
  private static final String PARAM_ACCESS_URL = "access_url";
  private static final int RETRY_TIMES = 5;

  private volatile Future[] tasks;
  private String fileSha;
  private String fileKey;
  private String uploadUrl;
  private String token;

  QCloudUploader(AVFile avFile, String token, String uploadUrl, ProgressCallback progressCallback) {
    super(avFile, progressCallback);

    this.fileKey = "";
    this.uploadUrl = uploadUrl;
    this.token = token;
  }

  private static final int DEFAULT_SLICE_LEN = 512 * 1024;

  public AVException execute() {
    try {
      byte[] bytes = avFile.getData();
      int sliceCount =
              (bytes.length / DEFAULT_SLICE_LEN) + (bytes.length % DEFAULT_SLICE_LEN == 0 ? 0 : 1);
      // 如果文件太小就没必要分片了
      if (sliceCount > 1) {
        JSONObject result = uploadControlSlice(token, uploadUrl, bytes);
        if (null == result) {
          return new AVException(new RuntimeException("Exception during file upload"));
        }
        if (result.containsKey(PARAM_ACCESS_URL)) {
          return null;
        }
        String sessionId = result.getString("session");

        FileUploader.ProgressCalculator progressCalculator =
                new FileUploader.ProgressCalculator(sliceCount, new FileUploader.FileUploadProgressCallback() {
                  @Override
                  public void onProgress(int progress) {
                    publishProgress(progress);
                  }
                });
        for (int sliceOffset = 0; sliceOffset < sliceCount && !AVExceptionHolder.exists(); sliceOffset++) {
          new SliceUploadTask(this, fileKey, token, uploadUrl, bytes,
                  sliceOffset,
                  sessionId, progressCalculator, null).upload();
        }
        if (AVExceptionHolder.exists()) {
          throw AVExceptionHolder.remove();
        }
      } else {
        uploadFile();
      }
    } catch (Exception e) {
      return new AVException(e);
    }

    return null;
  }

  private void uploadFile() throws AVException {

    try {
      byte[] bytes = FileCache.getIntance().readData(avFile.getName());// TODO: fix me!
      fileSha = AVUtils.SHA1(bytes);
      MultipartBody.Builder builder = new MultipartBody.Builder();
      RequestBody fileBody =
              RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), bytes, 0,
                      getCurrentSliceLength(0, bytes.length));
      builder.addFormDataPart(FILE_CONTENT, fileKey, fileBody);
      builder.addFormDataPart(PARAM_OP, OP_UPLOAD);
      builder.addFormDataPart(PARAM_SHA, fileSha);

      MediaType type = MediaType.parse(MULTIPART_FORM_DATA);
      if (null != type) {
        builder.setType(type);
      }

      Request.Builder requestBuilder = new Request.Builder();
      requestBuilder.url(uploadUrl);
      requestBuilder.header(HEADER_AUTHORIZATION, token);
      requestBuilder.header(HEADER_CONTENT_TYPE, MULTIPART_FORM_DATA);

      for(String key: FileUploader.UPLOAD_HEADERS.keySet()) {
        requestBuilder.header(key, FileUploader.UPLOAD_HEADERS.get(key));
      }

      requestBuilder.post(builder.build());

      Request request = requestBuilder.build();
      Response response = executeWithRetry(request, RETRY_TIMES);
      if (response.code() != 200) {
        throw AVErrorUtils.createException(AVException.OTHER_CAUSE,
                AVUtils.stringFromBytes(response.body().bytes()));
      }
    } catch (Exception e) {

      throw AVErrorUtils.createException(e, "Exception during file upload");
    }
  }

  private static JSONObject parseSliceUploadResponse(String resp) {
    if (!StringUtil.isEmpty(resp)) {
      try {
        com.alibaba.fastjson.JSONObject object = JSON.parseObject(resp);
        com.alibaba.fastjson.JSONObject data = object.getJSONObject("data");
        return data;
      } catch (Exception e) {
        ;
      }
    }
    return null;
  }

  private static int getCurrentSliceLength(int sliceCount, int totalSize) {
    int leftSize = totalSize - sliceCount * DEFAULT_SLICE_LEN;
    return leftSize >= DEFAULT_SLICE_LEN ? DEFAULT_SLICE_LEN : leftSize;
  }

  private JSONObject uploadControlSlice(String token, String url, byte[] wholeFile)
          throws AVException {
    MultipartBody.Builder builder = new MultipartBody.Builder();
    try {
      String fileSha = AVUtils.SHA1(wholeFile);
      builder.addFormDataPart(PARAM_SHA, fileSha);
      builder.addFormDataPart(PARAM_OP, OP_UPLOAD_SLICE);
      builder.addFormDataPart(PARAM_FILE_SIZE, String.valueOf(wholeFile.length));
      builder.addFormDataPart(PARAM_SLICE_SIZE, String.valueOf(DEFAULT_SLICE_LEN));

      MediaType type = MediaType.parse(MULTIPART_FORM_DATA);
      if (null != type) {
        builder.setType(type);
      }

      Request.Builder requestBuilder = new Request.Builder();
      requestBuilder.url(url);
      requestBuilder.header(HEADER_AUTHORIZATION, token);
      requestBuilder.header(HEADER_CONTENT_TYPE, MULTIPART_FORM_DATA);
      requestBuilder.post(builder.build());

      Request request = requestBuilder.build();
      Response response = executeWithRetry(request, RETRY_TIMES);
      if (response != null) {
        byte[] responseBody = response.body().bytes();
        return parseSliceUploadResponse(StringUtil.stringFromBytes(responseBody));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new AVException(AVException.OTHER_CAUSE, "Upload file failure");
    }
    return null;
  }

  public static class SliceUploadTask implements Runnable {
    byte[] data;
    int sliceOffset;
    FileUploader.ProgressCalculator progress;
    String session;
    CountDownLatch latch;
    String token;
    String url;
    String key;
    QCloudUploader parent;

    public SliceUploadTask(QCloudUploader parent, String key, String token, String url, byte[] wholeFile, int sliceOffset,
                           String session, FileUploader.ProgressCalculator progressCalculator, CountDownLatch latch) {
      this.data = wholeFile;
      this.sliceOffset = sliceOffset;
      this.progress = progressCalculator;
      this.session = session;
      this.latch = latch;
      this.token = token;
      this.url = url;
      this.key = key;
      this.parent = parent;
    }

    public void run() {
      this.upload();
    }

    public String upload() {
      try {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        RequestBody fileBody =
                RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), data, sliceOffset
                                * DEFAULT_SLICE_LEN,
                        getCurrentSliceLength(sliceOffset, data.length));
        builder.addFormDataPart(FILE_CONTENT, key, fileBody);
        builder.addFormDataPart(PARAM_OP, OP_UPLOAD_SLICE);
        builder.addFormDataPart(PARAM_OFFSET,
                String.valueOf(sliceOffset * DEFAULT_SLICE_LEN));
        builder.addFormDataPart(PARAM_SESSION, session);

        MediaType type = MediaType.parse(MULTIPART_FORM_DATA);
        if (null != type) {
          builder.setType(type);
        }

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        requestBuilder.header(HEADER_AUTHORIZATION, token);
        requestBuilder.header(HEADER_CONTENT_TYPE, MULTIPART_FORM_DATA);
        requestBuilder.post(builder.build());
        Request request = requestBuilder.build();
        Response response = parent.executeWithRetry(request, RETRY_TIMES);
        if (response != null) {
          byte[] responseBody = response.body().bytes();
          if (progress != null) {
            progress.publishProgress(sliceOffset, 100);
          }
          return StringUtil.stringFromBytes(responseBody);
        }
      } catch (Exception e) {
        AVExceptionHolder.add(new AVException(e));
        if (latch != null) {
          long count = latch.getCount();
          for (; count > 0; count--) {
            latch.countDown();
          }
        }
      }
      return null;
    }
  }

  @Override
  public void interruptImmediately() {
    super.interruptImmediately();

    if (tasks != null && tasks.length > 0) {
      synchronized (tasks) {
        for (int index = 0; index < tasks.length; index++) {
          Future task = tasks[index];
          if (task != null && !task.isDone() && !task.isCancelled()) {
            task.cancel(true);
          }
        }
      }
    }
  }
}
