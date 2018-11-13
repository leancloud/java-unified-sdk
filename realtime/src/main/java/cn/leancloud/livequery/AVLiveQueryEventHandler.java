package cn.leancloud.livequery;

import cn.leancloud.AVObject;
import cn.leancloud.AVUser;
import cn.leancloud.internal.ThreadModel;

import java.util.List;

public abstract class AVLiveQueryEventHandler {
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

  public void done(final AVLiveQuery.EventType eventType, final AVObject avObject, final List<String> updateKeyList) {
    switch (eventType) {
      case ENTER:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectEnter(avObject, updateKeyList);
            }
          });
        } else {
          onObjectEnter(avObject, updateKeyList);
        }
        break;
      case UPDATE:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectUpdated(avObject, updateKeyList);
            }
          });
        } else {
          onObjectUpdated(avObject, updateKeyList);
        }
        break;
      case DELETE:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectDeleted(avObject.getObjectId());
            }
          });
        } else {
          onObjectDeleted(avObject.getObjectId());
        }
        break;
      case LEAVE:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectLeave(avObject, updateKeyList);
            }
          });
        } else {
          onObjectLeave(avObject, updateKeyList);
        }
        break;
      case LOGIN:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              if (avObject instanceof AVUser) {
                onUserLogin((AVUser) avObject);
              }
            }
          });
        } else {
          if (avObject instanceof AVUser) {
            onUserLogin((AVUser) avObject);
          }
        }
        break;
      case CREATE:
        if (needCheckMainThread && !mainThreadChecker.isMainThread()) {
          threadShuttle.launch(new Runnable() {
            @Override
            public void run() {
              onObjectCreated(avObject);
            }
          });
        } else {
          onObjectCreated(avObject);
        }
        break;
      default:
        break;
    }
  }

  /**
   * This method will be called when an associated AVObject created
   * @param avObject
   */
  public void onObjectCreated(AVObject avObject) {}

  /**
   * This method will be called when an associated AVObject updated
   * @param avObject
   */
  public void onObjectUpdated(AVObject avObject, List<String> updateKeyList) {}

  /**
   * This method will be called when an AVObject matched the associated AVQuery after update
   * @param avObject
   */
  public void onObjectEnter(AVObject avObject, List<String> updateKeyList) {}

  /**
   * This method will be called when an AVObject is modified and does not conform to the relevant query
   * @param avObject
   */
  public void onObjectLeave(AVObject avObject, List<String> updateKeyList) {}

  /**
   * This method will be called when a related AVObject is deleted
   * @param objectId
   */
  public void onObjectDeleted(String objectId) {}

  /**
   * This method will be called when a related user login
   * @param user
   */
  public void onUserLogin(AVUser user) {}
}
