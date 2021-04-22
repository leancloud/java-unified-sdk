package cn.leancloud.livequery;

import cn.leancloud.LCObject;
import cn.leancloud.LCUser;
import cn.leancloud.internal.ThreadModel;

import java.util.List;

public abstract class LCLiveQueryEventHandler {
  private static volatile boolean needCheckMainThread = false;
  private static volatile ThreadModel.MainThreadChecker mainThreadChecker = null;
  private static volatile ThreadModel.ThreadShuttle threadShuttle = null;

  public static void setMainThreadChecker(ThreadModel.MainThreadChecker checker, ThreadModel.ThreadShuttle shuttle) {
    if (null == checker) {
      needCheckMainThread = false;
      mainThreadChecker = null;
      threadShuttle = null;
    } else {
      needCheckMainThread = true;
      mainThreadChecker = checker;
      threadShuttle = shuttle;
    }
  }

  public void done(final LCLiveQuery.EventType eventType, final LCObject LCObject, final List<String> updateKeyList) {
    switch (eventType) {
      case ENTER:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectEnter(LCObject, updateKeyList);
            }
          });
        } else {
          onObjectEnter(LCObject, updateKeyList);
        }
        break;
      case UPDATE:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectUpdated(LCObject, updateKeyList);
            }
          });
        } else {
          onObjectUpdated(LCObject, updateKeyList);
        }
        break;
      case DELETE:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectDeleted(LCObject.getObjectId());
            }
          });
        } else {
          onObjectDeleted(LCObject.getObjectId());
        }
        break;
      case LEAVE:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectLeave(LCObject, updateKeyList);
            }
          });
        } else {
          onObjectLeave(LCObject, updateKeyList);
        }
        break;
      case LOGIN:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              if (LCObject instanceof LCUser) {
                onUserLogin((LCUser) LCObject);
              }
            }
          });
        } else {
          if (LCObject instanceof LCUser) {
            onUserLogin((LCUser) LCObject);
          }
        }
        break;
      case CREATE:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectCreated(LCObject);
            }
          });
        } else {
          onObjectCreated(LCObject);
        }
        break;
      default:
        break;
    }
  }

  /**
   * This method will be called when an associated AVObject created
   * @param LCObject object instance.
   */
  public void onObjectCreated(LCObject LCObject) {}

  /**
   * This method will be called when an associated AVObject updated
   * @param LCObject object instance.
   * @param updateKeyList updated key list.
   */
  public void onObjectUpdated(LCObject LCObject, List<String> updateKeyList) {}

  /**
   * This method will be called when an AVObject matched the associated AVQuery after update
   * @param LCObject object instance.
   * @param updateKeyList updated key list.
   */
  public void onObjectEnter(LCObject LCObject, List<String> updateKeyList) {}

  /**
   * This method will be called when an AVObject is modified and does not conform to the relevant query
   * @param LCObject object instance.
   * @param updateKeyList  updated key list.
   */
  public void onObjectLeave(LCObject LCObject, List<String> updateKeyList) {}

  /**
   * This method will be called when a related AVObject is deleted
   * @param objectId object id.
   */
  public void onObjectDeleted(String objectId) {}

  /**
   * This method will be called when a related user login
   * @param user user instance.
   */
  public void onUserLogin(LCUser user) {}
}
