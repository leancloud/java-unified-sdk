package cn.leancloud;

import cn.leancloud.codec.MD5;
import cn.leancloud.ops.BaseOperation;
import cn.leancloud.ops.BaseOperationAdapter;
import cn.leancloud.ops.ObjectFieldOperation;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializeConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArchivedRequests {
  private static final String ATTR_METHOD = "method";
  private static final String METHOD_DELETE = "Delete";
  private static final String METHOD_SAVE = "Save";
  private static final String ATTR_INTERNAL_ID = "internalId";
  private static final String ATTR_OBJECT = "objectJson";
  private static final String ATTR_OPERATION = "opertions";

  private static ArchivedRequests instance = null;
  public static synchronized ArchivedRequests getInstance() {
    if (null == instance) {
      instance = new ArchivedRequests();
    }
    return instance;
  }
  private Map<String, AVObject> saveObjects = new HashMap<>();
  private Map<String, AVObject> deleteObjects = new HashMap<>();

  private ArchivedRequests() {
    ;
  }

  public void saveEventually(AVObject object) {
    if (null == object) {
      return;
    }
    saveObjects.put(object.internalId(), object);
  }

  public void deleteEventually(AVObject object) {
    if (null == object) {
      return;
    }

    deleteObjects.put(object.internalId(), object);
  }

  public static String getArchiveContent(AVObject object, boolean isDelete) {
    Map<String, String> content = new HashMap<>(3);
    content.put(ATTR_METHOD, isDelete ? METHOD_DELETE : METHOD_SAVE);
    content.put(ATTR_INTERNAL_ID, object.internalId());
    content.put(ATTR_OBJECT, JSON.toJSONString(object));
    content.put(ATTR_OPERATION, JSON.toJSONString(object.operations.values()));

    return JSON.toJSONString(content);
  }

  public static AVObject parse2Object(String content) {
    Map<String, String> contentMap = JSON.parseObject(content, Map.class);
    String method = contentMap.get(ATTR_METHOD);
    String internalId = contentMap.get(ATTR_INTERNAL_ID);
    String objectJSON = contentMap.get(ATTR_OBJECT);
    String operationJSON = contentMap.get(ATTR_OPERATION);
    AVObject resultObj = JSON.parseObject(objectJSON, AVObject.class);
    if (!StringUtil.isEmpty(internalId) && !internalId.equals(resultObj.getObjectId())) {
      resultObj.setUuid(internalId);
    }
    if (!StringUtil.isEmpty(operationJSON)) {
      List<BaseOperation> ops = JSON.parseObject(operationJSON,
              new TypeReference<List<BaseOperation>>() {});
      for (BaseOperation op: ops) {
        resultObj.addNewOperation(op);
      }
    }
    return resultObj;
  }

  private static String getArchiveRequestFileName(AVObject object) {
    if (StringUtil.isEmpty(object.getObjectId())) {
      return object.internalId();
    } else {
      return MD5.computeMD5(object.getRequestRawEndpoint());
    }
  }
}
