package cn.leancloud.testcase;

import cn.leancloud.AVFile;
import cn.leancloud.DemoBaseActivity;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by fengjunwen on 2018/3/22.
 */

public class FileDemoActivity extends DemoBaseActivity {
  public void testUploader() throws Exception {
    String contents = StringUtil.getRandomString(64);
    AVFile file = new AVFile("test", contents.getBytes());
    Observable<AVFile> result = file.saveInBackground();
    result.subscribe(new Observer<AVFile>() {
      @Override
      public void onSubscribe(Disposable d) {
        ;
      }

      @Override
      public void onNext(AVFile avFile) {
        log("Thread:" + Thread.currentThread().getId());
        log("保存了一个File：" + avFile.getObjectId());
      }

      @Override
      public void onError(Throwable e) {

      }

      @Override
      public void onComplete() {

      }
    });
  }
}
