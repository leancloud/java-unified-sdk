package cn.leancloud.upload;

import cn.leancloud.LCException;
import cn.leancloud.callback.ProgressCallback;
import cn.leancloud.codec.SHA1;
import cn.leancloud.LCFile;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import okhttp3.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

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

  private AtomicReferenceArray<Future> tasks;
  private String fileSha;
  private String fileKey;
  private String uploadUrl;
  private String token;

  QCloudUploader(LCFile avFile, String token, String uploadUrl, ProgressCallback progressCallback) {
    super(avFile, progressCallback);

    this.fileKey = avFile.getKey();
    this.uploadUrl = uploadUrl;
    this.token = token;
  }

  private static final int DEFAULT_SLICE_LEN = 512 * 1024;

  public LCException execute() {
    try {
      byte[] bytes = avFile.getData();
      int sliceCount =
              (bytes.length / DEFAULT_SLICE_LEN) + (bytes.length % DEFAULT_SLICE_LEN == 0 ? 0 : 1);

      // 如果文件太小就没必要分片了
      if (sliceCount > 1) {
        JSONObject result = uploadControlSlice(token, uploadUrl, bytes);
        if (null == result) {
          return new LCException(new RuntimeException("Exception during file upload"));
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
        String sliceUploadResult = "";
        int sliceOffset = 0;
        for (; sliceOffset < sliceCount && null != sliceUploadResult; sliceOffset++) {
          sliceUploadResult = new SliceUploadTask(this, fileKey, token, uploadUrl,
                  bytes, sliceOffset, sessionId, progressCalculator, null).upload();
        }
        if (sliceOffset < sliceCount) {
          return new LCException(LCException.OTHER_CAUSE, "failed to upload slice.");
        }
      } else {
        uploadFile(bytes);
      }
    } catch (Exception e) {
      return new LCException(e);
    }

    return null;
  }

  private void uploadFile(byte[] bytes) throws LCException {

    try {
      fileSha = SHA1.compute(bytes);
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

      for (Map.Entry<String, String> entry: FileUploader.UPLOAD_HEADERS.entrySet()) {
        requestBuilder.header(entry.getKey(), entry.getValue());
      }

      requestBuilder.post(builder.build());

      Request request = requestBuilder.build();
      Response response = executeWithRetry(request, RETRY_TIMES);
      if (response.code() != 200) {
        throw new LCException(LCException.OTHER_CAUSE,
                StringUtil.stringFromBytes(response.body().bytes()));
      }
    } catch (Exception e) {

      throw new LCException("Exception during file upload", e);
    }
  }

  private static JSONObject parseSliceUploadResponse(String resp) {
    if (!StringUtil.isEmpty(resp)) {
      try {
        JSONObject object = JSON.parseObject(resp);
        JSONObject data = object.getJSONObject("data");
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
          throws LCException {
    MultipartBody.Builder builder = new MultipartBody.Builder();
    try {
      String fileSha = SHA1.compute(wholeFile);
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
      throw new LCException(LCException.OTHER_CAUSE, "Upload file failure");
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
        if (response != null && null != response.body()) {
          byte[] responseBody = response.body().bytes();
          if (progress != null) {
            progress.publishProgress(sliceOffset, 100);
          }
          if (null != responseBody) {
            return StringUtil.stringFromBytes(responseBody);
          }
        }
      } catch (Exception e) {
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

    if (tasks != null && tasks.length() > 0) {
      synchronized (tasks) {
        for (int index = 0; index < tasks.length(); index++) {
          Future task = tasks.get(index);
          if (task != null && !task.isDone() && !task.isCancelled()) {
            task.cancel(true);
          }
        }
      }
    }
  }
}
