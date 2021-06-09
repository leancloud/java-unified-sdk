package cn.leancloud.sample.testcase;

import cn.leancloud.LCCloudQuery;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.query.LCCloudQueryResult;

/**
 * Created by fengjunwen on 2018/5/10.
 */

public class CQLDemoActivity extends DemoBaseActivity {
  void logResult(String cql, LCCloudQueryResult result) {
    log("cql:%s\nresult:%s", cql, prettyJSON(result.getResults()));
  }

  void logCount(String cql, LCCloudQueryResult result) {
    log("cql:%s\ncount:%d", cql, result.getCount());
  }

  public void testSelect() throws Exception {
    String cql = "select * from _User";
    LCCloudQueryResult result = LCCloudQuery.executeInBackground(cql).blockingFirst();
    logResult(cql, result);
  }

  public void testCount() throws Exception {
    String cql = "select count(*) from _User";
    LCCloudQueryResult result = LCCloudQuery.executeInBackground(cql).blockingFirst();
    logCount(cql, result);
  }

  public void testSelectWhere() throws Exception {
    String cql = String.format("select * from _User where username=?");
    LCCloudQueryResult result = LCCloudQuery.executeInBackground(cql, "XiaoMing").blockingFirst();
    logResult(cql, result);
  }

  public void testSelectWhereIn() throws Exception {
    String cql = String.format("select * from _User where username in (?,?)");
    LCCloudQueryResult result = LCCloudQuery.executeInBackground(cql, "XiaoMing", "lzwjava@gmail.com").blockingFirst();
    logResult(cql, result);
  }

  public void testSelectWhereDate() throws Exception {
    String cql = String.format("select * from _User where createdAt<date(?) order by -createdAt limit ?");
    LCCloudQueryResult result = LCCloudQuery.executeInBackground(cql, "2015-05-01T00:00:00.0000Z", "3").blockingFirst();
    logResult(cql, result);
  }
}
