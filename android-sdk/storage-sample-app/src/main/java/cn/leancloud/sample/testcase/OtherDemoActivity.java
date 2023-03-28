package cn.leancloud.sample.testcase;

import cn.leancloud.LCException;
import cn.leancloud.LCQuery;
import cn.leancloud.LeanCloud;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.sample.Student;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.types.LCDate;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by fengjunwen on 2018/5/10.
 */

public class OtherDemoActivity extends DemoBaseActivity {
  public void testGetSereverDate() throws LCException {
    LeanCloud.getServerDateInBackground().subscribe(new Observer<LCDate>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(LCDate lcDate) {
        log("服务器时间：" + lcDate);
      }

      @Override
      public void onError(Throwable e) {

      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testConfigNetworkTimeout() throws LCException {
    // 得放到 Application 里
    LeanCloud.setNetworkTimeout(10);
    try {
      Student student = getFirstStudent();
      log("student:" + prettyJSON(student));
    } catch (LCException e) {
      log("因为设置了网络超时为 10 毫秒，所以超时了，e:" + e.getMessage());
    }
    LeanCloud.setNetworkTimeout(AppConfiguration.DEFAULT_NETWORK_TIMEOUT);
  }

}
