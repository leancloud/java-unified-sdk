package cn.leancloud.sample.testcase;

import cn.leancloud.AVQuery;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.query.AVCloudQueryResult;

/**
 * Created by fengjunwen on 2018/5/10.
 */

public class CQLDemoActivity extends DemoBaseActivity {
  void logResult(String cql, AVCloudQueryResult result) {
    log("cql:%s\nresult:%s", cql, prettyJSON(result.getResults()));
  }

  void logCount(String cql, AVCloudQueryResult result) {
    log("cql:%s\ncount:%d", cql, result.getCount());
  }

  public void testSelect() throws Exception {
    String cql = "select * from _User";
    AVCloudQueryResult result = AVQuery.doCloudQueryInBackground(cql).blockingFirst();
    logResult(cql, result);
  }

  public void testCount() throws Exception {
    String cql = "select count(*) from _User";
    AVCloudQueryResult result = AVQuery.doCloudQueryInBackground(cql).blockingFirst();
    logCount(cql, result);
  }

  public void testSelectWhere() throws Exception {
    String cql = String.format("select * from _User where username=?");
    AVCloudQueryResult result = AVQuery.doCloudQueryInBackground(cql, "XiaoMing").blockingFirst();
    logResult(cql, result);
  }

  public void testSelectWhereIn() throws Exception {
    String cql = String.format("select * from _User where username in (?,?)");
    AVCloudQueryResult result = AVQuery.doCloudQueryInBackground(cql, "XiaoMing", "lzwjava@gmail.com").blockingFirst();
    logResult(cql, result);
  }

  public void testSelectWhereDate() throws Exception {
    String cql = String.format("select * from _User where createdAt<date(?) order by -createdAt limit ?");
    AVCloudQueryResult result = AVQuery.doCloudQueryInBackground(cql, "2015-05-01T00:00:00.0000Z", "3").blockingFirst();
    logResult(cql, result);
  }
}
