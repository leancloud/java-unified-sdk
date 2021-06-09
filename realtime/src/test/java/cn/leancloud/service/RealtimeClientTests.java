package cn.leancloud.service;

import cn.leancloud.Configure;
import cn.leancloud.im.v2.conversation.LCIMConversationMemberInfo;
import cn.leancloud.query.QueryConditions;
import cn.leancloud.query.QueryOperation;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RealtimeClientTests extends TestCase {
  private boolean operationSucceed = false;

  public RealtimeClientTests(String name) {
    super(name);
    Configure.initialize();
  }

  @Override
  protected void setUp() throws Exception {
    operationSucceed = false;
  }

  public void testQueryMemberInfo() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    String rtmSessionToken = "RiILshUnfFJu2eQM-gzGzoHszVAwpx2AXSv27tutGw7uFgA";

    QueryConditions conditions = new QueryConditions();
    conditions.addWhereItem("cid", QueryOperation.EQUAL_OP, "5f277c3f47f3e2167b3f5e02");
    conditions.setSkip(0);
    conditions.setLimit(10);

    conditions.assembleParameters();
    Map<String, String> queryParams = conditions.getParameters();
    queryParams.put("client_id", "a@b.c");

    RealtimeClient.getInstance().queryMemberInfo(queryParams, rtmSessionToken).subscribe(new Observer<List<LCIMConversationMemberInfo>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<LCIMConversationMemberInfo> LCIMConversationMemberInfos) {
        if (null != LCIMConversationMemberInfos) {
          for (LCIMConversationMemberInfo info: LCIMConversationMemberInfos) {
            System.out.println("MemberInfo: " + info.toString());
          }
        }
        operationSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }
}
