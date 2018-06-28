package cn.leancloud.sample.testcase;

import java.util.HashMap;

import cn.leancloud.AVCloud;
import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.sample.Student;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by fengjunwen on 2018/5/10.
 */

public class EngineDemoActivity extends DemoBaseActivity {
  public void testCallCloudFunction() throws AVException {
    Object hello = AVCloud.callFunctionInBackground("hello", null).blockingFirst();
    log("云引擎返回的结果:" + hello);
    logThreadTips();
  }

  public void testErrorCode() throws AVException {
    AVCloud.callFunctionInBackground("errorCode", null).subscribe(new Observer<Object>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(Object o) {

      }

      @Override
      public void onError(Throwable e) {
        if (e instanceof AVException) {
          AVException ave = (AVException) e;
          if (ave.getCode() == 211) {
            log("云引擎返回的 Error, code：" + ave.getCode() + " message:" + ave.getMessage());
            return;
          }
        }
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testCustomErrorCode() throws AVException {
//    try {
      AVCloud.callFunctionInBackground("customErrorCode", null).blockingSubscribe();
//    } catch (AVException e) {
//      if (e.getCode() == 123) {
//        log("云引擎返回的 Error, code：" + e.getCode() + " message:" + e.getMessage());
//      } else {
//        throw e;
//      }
//    }
  }

  public void testFetchObject() throws AVException {
    Student student = getFirstStudent();
    HashMap<String, Object> params = new HashMap<>();

    Student fetchStudent = Student.createWithoutData(Student.class, student.getObjectId());
    params.put("obj", student);
    Object fetchObject = AVCloud.callFunctionInBackground("fetchObject", params).blockingFirst();
    log("根据返回结果构造的对象:" + fetchObject);
  }

  public void testFullObject() throws AVException {
    Object object = AVCloud.callFunctionInBackground("fullObject", null).blockingFirst();
    log("从云引擎中获取整个对象:" + object);
  }

  public void testBeforeSave() throws AVException {
    AVObject object = new AVObject("AVCloudTest");
    object.put("string", "This is too much long, too much long, too long");
    object.setFetchWhenSave(true);
    object.save();
    log("通过 beforeSave Hook 截断至 10个字符:" + object.getString("string"));
  }
}
