package cn.leancloud;

import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.codec.MDFive;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.network.NetworkingDetector;
import cn.leancloud.ops.BaseOperation;
import cn.leancloud.types.LCNull;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;

import cn.leancloud.json.JSON;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.io.File;
import java.util.*;

public class ArchivedRequests {
  private static final LCLogger logger = LogUtil.getLogger(ArchivedRequests.class);
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
  private Map<String, LCObject> saveObjects = new HashMap<>();
  private Map<String, LCObject> deleteObjects = new HashMap<>();
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

  private void sendArchivedRequest(final Map<String, LCObject> collection, final boolean isDelete) {
    if (null == collection || collection.isEmpty()) {
      return;
    }
    Collection<LCObject> objects = collection.values();
    int opCount = 0;
    int opLimit = objects.size() > 5 ? 5 : objects.size();
    Iterator<LCObject> iterator = objects.iterator();
    List<LCObject> opTargets = new ArrayList<>(opLimit);
    while(opCount < opLimit && iterator.hasNext()) {
      LCObject obj = iterator.next();
      opCount++;
      opTargets.add(obj);
    }
    for (final LCObject obj: opTargets) {
      if (isDelete) {
        obj.deleteInBackground().subscribe(new Observer<LCNull>() {
          @Override
          public void onSubscribe(Disposable disposable) { }

          @Override
          public void onNext(LCNull LCNull) {
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
        final String objectInternalId = obj.internalId();
        obj.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) { }

          @Override
          public void onNext(LCObject LCObject) {
            collection.remove(objectInternalId);
            File archivedFile = getArchivedFile(objectInternalId, isDelete);
            boolean ret = PersistenceUtil.sharedInstance().forceDeleteFile(archivedFile);
            if (!ret) {
              logger.w("failed to delete file:" + archivedFile.getAbsolutePath() + " for objectInternalId: " + objectInternalId);
            } else {
              logger.d("succeed to delete file:" + archivedFile.getAbsolutePath() + " for objectInternalId: " + objectInternalId);
            }
          }

          @Override
          public void onError(Throwable throwable) {
            if (throwable instanceof LCException) {
              LCException lcException = (LCException) throwable;
              int status = lcException.getHttpStatus();
              logger.w("failed to save archived request. cause: " + lcException.getMessage() + ", code: " + lcException.getCode() + ", status: " + status);
              if (status == 0 || status == 429 || status >= 499) {
                return;
              }
              onNext(null);
            } else {
              logger.w("failed to save archived request. cause: ", throwable);
            }
          }

          @Override
          public void onComplete() { }
        });
      }
    }
  }

  public void saveEventually(LCObject object) {
    if (null == object) {
      return;
    }
    saveArchivedRequest(object, false);
    saveObjects.put(object.internalId(), object);
  }

  public void deleteEventually(LCObject object) {
    if (null == object) {
      return;
    }
    saveArchivedRequest(object, true);
    deleteObjects.put(object.internalId(), object);
  }

  private File getArchivedFile(LCObject object, boolean isDelete) {
    String fileName = getArchiveRequestFileName(object);
    return new File(AppConfiguration.getCommandCacheDir(), fileName);
  }
  private File getArchivedFile(String fileName, boolean isDelete) {
    return new File(AppConfiguration.getCommandCacheDir(), fileName);
  }

  private void saveArchivedRequest(LCObject object, boolean isDelete) {
    String content = getArchiveContent(object, isDelete);
    File targetFile = getArchivedFile(object, isDelete);
    PersistenceUtil.sharedInstance().saveContentToFile(content, targetFile);
  }

  public static String getArchiveContent(LCObject object, boolean isDelete) {
    Map<String, String> content = new HashMap<>(3);
    content.put(ATTR_METHOD, isDelete ? METHOD_DELETE : METHOD_SAVE);
    content.put(ATTR_INTERNAL_ID, object.internalId());
    content.put(ATTR_OBJECT, object.toJSONString());
    content.put(ATTR_OPERATION, JSON.toJSONString(object.operations.values()));

    return JSON.toJSONString(content);
  }

  private void parseArchiveFile(File file) {
    if (null == file) {
      return;
    }
    if (!LCObject.verifyInternalId(file.getName())) {
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
      LCObject resultObj = parseAVObject(contentMap);
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
  protected static LCObject parseAVObject(String content) {
    Map<String, String> contentMap = JSON.parseObject(content, Map.class);
    return parseAVObject(contentMap);
  }

  private static LCObject parseAVObject(Map<String, String> contentMap) {
    String internalId = contentMap.get(ATTR_INTERNAL_ID);
    String objectJSON = contentMap.get(ATTR_OBJECT);
    String operationJSON = contentMap.get(ATTR_OPERATION);

    LCObject resultObj = LCObject.parseLCObject(objectJSON);
    if (!StringUtil.isEmpty(internalId) && !internalId.equals(resultObj.getObjectId())) {
      resultObj.setUuid(internalId);
    }
    if (!StringUtil.isEmpty(operationJSON)) {
      List<BaseOperation> ops = JSON.parseArray(operationJSON, BaseOperation.class);
      for (BaseOperation op: ops) {
        resultObj.addNewOperation(op);
      }
    }
    return resultObj;
  }

  private static String getArchiveRequestFileName(LCObject object) {
    if (!StringUtil.isEmpty(object.getObjectId())) {
      return object.getObjectId();
    } else if (!StringUtil.isEmpty(object.getUuid())) {
      return object.getUuid();
    } else {
      return MDFive.computeMD5(object.getRequestRawEndpoint());
    }
  }
}
