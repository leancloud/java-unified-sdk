package cn.leancloud.testcase;

import java.util.Date;

import cn.leancloud.AVException;
import cn.leancloud.AVOSCloud;
import cn.leancloud.DemoBaseActivity;
import cn.leancloud.Student;
import cn.leancloud.core.AppConfiguration;

/**
 * Created by fengjunwen on 2018/5/10.
 */

public class OtherDemoActivity extends DemoBaseActivity {
//  public void testGetSereverDate() throws AVException {
//    Date date = AVOSCloud.getServerDate();
//    log("服务器时间：" + date);
//  }

  public void testConfigNetworkTimeout() throws AVException {
    // 得放到 Application 里
    AVOSCloud.setNetworkTimeout(10);
    try {
      Student student = getFirstStudent();
      log("student:" + prettyJSON(student));
    } catch (AVException e) {
      log("因为设置了网络超时为 10 毫秒，所以超时了，e:" + e.getMessage());
    }
    AVOSCloud.setNetworkTimeout(AppConfiguration.DEFAULT_NETWORK_TIMEOUT);
  }

}
