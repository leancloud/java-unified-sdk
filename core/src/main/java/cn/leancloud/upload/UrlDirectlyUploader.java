package cn.leancloud.upload;

import cn.leancloud.AVException;
import cn.leancloud.ProgressCallback;
import cn.leancloud.SaveCallback;
import cn.leancloud.core.AVFile;
import cn.leancloud.network.PaasClient;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UrlDirectlyUploader extends HttpClientUploader {
  private UploadCallback callback = null;
  public UrlDirectlyUploader(AVFile avFile, SaveCallback saveCallback,
                             ProgressCallback progressCallback, UploadCallback uploadCallback) {
    super(avFile, saveCallback, progressCallback);
    this.callback = uploadCallback;
  }

  @Override
  public AVException doWork() {

    final AVException[] exceptionSaveFile = new AVException[1];
    String url = AVPowerfulUtils.getEndpoint(avFile, true);
    String params = getFileRequestParameters();
    PaasClient.storageInstance().postObject(url, params, true,
            new GenericObjectCallback() {
              @Override
              public void onSuccess(String content, AVException e) {
                if (e == null) {
                  try {
                    JSONObject jsonObject = new JSONObject(content);
                    if (null != callback) {
                      callback.finishedWithResults(jsonObject.getString("objectId"), avFile.getUrl());
                    }
//                avFile.handleUploadedResponse(jsonObject.getString("objectId"),
//                    jsonObject.getString("objectId"), avFile.getUrl());
                    publishProgress(100);
                  } catch (Exception ex) {
                    exceptionSaveFile[0] = new AVException(ex);
                  }
                } else {
                  exceptionSaveFile[0] = AVErrorUtils.createException(e, content);
                }
              }

              @Override
              public void onFailure(Throwable error, String content) {
                exceptionSaveFile[0] = AVErrorUtils.createException(error, content);
              }

            });

    return exceptionSaveFile[0];
  }

  private String getFileRequestParameters() {
    // decide file mimetype.
    String mimeType = AVFileUtil.getFileMimeType(avFile);

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("name", avFile.getName());
    parameters.put("mime_type", mimeType);
    parameters.put("metaData", avFile.getMetaData());
    parameters.put("__type", AVFile.className());
    parameters.put("url", avFile.getUrl());
    if (avFile.getACL() != null) {
      parameters.putAll(AVUtils.getParsedMap(avFile.getACL().getACLMap()));
    }
    return AVUtils.restfulServerData(parameters);
  }

}