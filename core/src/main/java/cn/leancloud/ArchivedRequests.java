package cn.leancloud;

import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.codec.MD5;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.network.NetworkingDetector;
import cn.leancloud.ops.BaseOperation;
import cn.leancloud.ops.BaseOperationAdapter;
import cn.leancloud.ops.ObjectFieldOperation;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.observable.ObservableError;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.fastjson.parser.Feature.IgnoreAutoType;
import static com.alibaba.fastjson.parser.Feature.IgnoreNotMatch;
import static com.alibaba.fastjson.parser.Feature.SupportAutoType;

public class ArchivedRequests {
  private static final AVLogger logger = LogUtil.getLogger(ArchivedRequests.class);
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
  private Timer timer = null;

  private ArchivedRequests() {
    String commandCacheDir = AppConfiguration.getCommandCacheDir();
    List<File> files = PersistenceUtil.sharedInstance().listFiles(commandCacheDir);
    for (File f: files) {
      parseArchiveFile(f);
    }
    // begin timer.
    timer = new Timer(true);
    TimerTask task = new TimerTask() {
      public void run() {
        logger.i("begin to run timer task for archived request.");
        NetworkingDetector detector = AppConfiguration.getGlobalNetworkingDetector();
        if (null == detector || !detector.isConnected()) {
          logger.i("ignore timer task bcz networking is unavailable.");
          return;
        }
        if (saveObjects.isEmpty() && deleteObjects.isEmpty()) {
          logger.i("ignore timer task bcz request queue is empty.");
          return;
        }
        if (saveObjects.size() > 0) {
          sendArchivedRequest(saveObjects, false);
        }
        if (deleteObjects.size() > 0) {
          sendArchivedRequest(deleteObjects, true);
        }
        logger.i("end to run timer task for archived request.");
      }
    };
    timer.schedule(task, 10000, 15000);
  }

  private void sendArchivedRequest(final Map<String, AVObject> collection, final boolean isDelete) {
    if (null == collection || collection.isEmpty()) {
      return;
    }
    Collection<AVObject> objects = collection.values();
    int opCount = 0;
    int opLimit = objects.size() > 5 ? 5 : objects.size();
    Iterator<AVObject> iterator = objects.iterator();
    while(opCount < opLimit && iterator.hasNext()) {
      final AVObject obj = iterator.next();
      opCount++;
      if (isDelete) {
        obj.deleteInBackground().subscribe(new Observer<AVNull>() {
          @Override
          public void onSubscribe(Disposable disposable) { }

          @Override
          public void onNext(AVNull avNull) {
            collection.remove(obj.internalId());
            File archivedFile = getArchivedFile(obj, isDelete);
            boolean ret = PersistenceUtil.sharedInstance().forceDeleteFile(archivedFile);
            if (!ret) {
              logger.w("failed to delete file:" + archivedFile.getAbsolutePath() + " for objectInternalId: " + obj.internalId());
            } else {
              logger.d("succeed to delete file:" + archivedFile.getAbsolutePath() + " for objectInternalId: " + obj.internalId());
            }
          }

          @Override
          public void onError(Throwable throwable) {
            logger.w("failed to delete archived request. cause: ", throwable);
          }

          @Override
          public void onComplete() { }
        });
      } else {
        obj.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) { }

          @Override
          public void onNext(AVObject avObject) {
            collection.remove(obj.internalId());
            File archivedFile = getArchivedFile(obj, isDelete);
            boolean ret = PersistenceUtil.sharedInstance().forceDeleteFile(archivedFile);
            if (!ret) {
              logger.w("failed to delete file:" + archivedFile.getAbsolutePath() + " for objectInternalId: " + obj.internalId());
            } else {
              logger.d("succeed to delete file:" + archivedFile.getAbsolutePath() + " for objectInternalId: " + obj.internalId());
            }
          }

          @Override
          public void onError(Throwable throwable) {
            logger.w("failed to save archived request. cause: ", throwable);
          }

          @Override
          public void onComplete() { }
        });
      }
    }
  }

  public void saveEventually(AVObject object) {
    if (null == object) {
      return;
    }
    saveArchivedRequest(object, false);
    saveObjects.put(object.internalId(), object);
  }

  public void deleteEventually(AVObject object) {
    if (null == object) {
      return;
    }
    saveArchivedRequest(object, true);
    deleteObjects.put(object.internalId(), object);
  }

  private File getArchivedFile(AVObject object, boolean isDelete) {
    String fileName = getArchiveRequestFileName(object);
    return new File(AppConfiguration.getCommandCacheDir(), fileName);
  }

  private void saveArchivedRequest(AVObject object, boolean isDelete) {
    String content = getArchiveContent(object, isDelete);
    File targetFile = getArchivedFile(object, isDelete);
    PersistenceUtil.sharedInstance().saveContentToFile(content, targetFile);
  }

  public static String getArchiveContent(AVObject object, boolean isDelete) {
    Map<String, String> content = new HashMap<>(3);
    content.put(ATTR_METHOD, isDelete ? METHOD_DELETE : METHOD_SAVE);
    content.put(ATTR_INTERNAL_ID, object.internalId());
    content.put(ATTR_OBJECT, object.toJSONString());
    content.put(ATTR_OPERATION, JSON.toJSONString(object.operations.values(), ObjectValueFilter.instance,
            /*SerializerFeature.WriteClassName, */SerializerFeature.QuoteFieldNames,
            SerializerFeature.DisableCircularReferenceDetect));

    return JSON.toJSONString(content);
  }

  private void parseArchiveFile(File file) {
    if (null == file) {
      return;
    }
    if (!AVObject.verifyInternalId(file.getName())) {
      logger.d("ignore invalid file. " + file.getAbsolutePath());
      return;
    }

    String content = PersistenceUtil.sharedInstance().readContentFromFile(file);
    if (StringUtil.isEmpty(content)) {
      return;
    }
    try {
      Map<String, String> contentMap = JSON.parseObject(content, Map.class);
      String method = contentMap.get(ATTR_METHOD);
      AVObject resultObj = parseAVObject(contentMap);
      logger.d("get archived request. method=" + method + ", object=" + resultObj.toString());
      if (METHOD_SAVE.equalsIgnoreCase(method)) {
        saveObjects.put(resultObj.internalId(), resultObj);
      } else {
        deleteObjects.put(resultObj.internalId(), resultObj);
      }
    } catch (Exception ex) {
      logger.w("encounter exception whiling parse archived file.", ex);
    }
  }

  // just for serializer test.
  protected static AVObject parseAVObject(String content) {
    Map<String, String> contentMap = JSON.parseObject(content, Map.class);
    return parseAVObject(contentMap);
  }

  private static AVObject parseAVObject(Map<String, String> contentMap) {
    String internalId = contentMap.get(ATTR_INTERNAL_ID);
    String objectJSON = contentMap.get(ATTR_OBJECT);
    String operationJSON = contentMap.get(ATTR_OPERATION);

    AVObject resultObj = AVObject.parseAVObject(objectJSON);
    if (!StringUtil.isEmpty(internalId) && !internalId.equals(resultObj.getObjectId())) {
      resultObj.setUuid(internalId);
    }
    if (!StringUtil.isEmpty(operationJSON)) {
      List<BaseOperation> ops = JSON.parseObject(operationJSON,
              new TypeReference<List<BaseOperation>>() {}, IgnoreNotMatch);
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
