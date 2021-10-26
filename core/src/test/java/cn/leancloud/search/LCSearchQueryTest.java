package cn.leancloud.search;

import cn.leancloud.LCObject;
import cn.leancloud.Configure;
import cn.leancloud.LCQuery;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class LCSearchQueryTest extends TestCase {
  private boolean testSucceed = false;
  private CountDownLatch latch = null;
  public LCSearchQueryTest(String name) {
    super(name);
    Configure.initializeRuntime();
    try {
      prepareTickets();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static Test suite() {
    return new TestSuite(LCSearchQueryTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    testSucceed = false;
    latch = new CountDownLatch(1);
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }

  private void prepareTickets() throws Exception {
    String content = "我现在vivo手机能通过自定义receiver接收到消息，说明透传功能是OK的？\n" +
            "正常情况 vivo 混合推送是不能收到透传消息的，正常的 vivo 推送流程是您在后台发推送，然后通知栏显示一条推送，用户点击通知栏消息，应用内会响应点击事件。这个就是全部 vivo 推送支持的内容。\n" +
            "\n" +
            "您这边说自定义 receiver 接收到消息了，请问消息是什么格式的，接收到消息的完整日志能否提供一下。\n" +
            "\n" +
            "我现在应用是在前台，这时候跟混合推送没有关系吧？\n" +
            "您的问题和应用在前台、后台没有关系。\n" +
            "\n" +
            "现在的问题是初始化了混合推送影响了我notification的notificationchannel的开关，怎么解决这个bug?而不是说走通知消息解决。即使走通知消息，你这个混合推送初始化还是影响了我这边自定义通知的渠道\n" +
            "我们现在就是在调查解决这个问题啊。\n" +
            "您完全按照 vivo 的文档集成一遍，然后根据 Installation ID 推送能否收到通知栏消息呢。您刚才再次测试过的推送 ID 提供一下。\n" +
            "\n" +
            "第二：我问过我们后台服务器，这边设置了silent=false,客户端还是通过自定义的receiver接收消息\n" +
            "推送服务通过推送请求中 data 参数内的 silent 字段区分透传和通知栏消息。 silent 为 true 表示这个消息是透传消息，为 false 表示消息是通知栏消息。 如果不传递 silent 则默认其值为 false。\n" +
            "https://leancloud.cn/docs/push-rest-api.html#hash79355699\n" +
            "\n" +
            "\"action\": \"com.heishi.UPDATE_STATUS\",\n" +
            "推送的时候这边不能设置 action 参数，有这个参数也会默认去找对应的 Receiver。vivo 不支持自定义 Receiver，所以发推送要去掉 action。";
    String author = "testUser";
    int contentLength = content.length();
    Random rand = new Random();
    LCQuery query = new LCQuery("Ticket");
    int ticketCnt = query.count();
    if (ticketCnt > 15) {
      return;
    }
    for (int i = 0; i < 20; i++) {
      int start = rand.nextInt(contentLength/2);
      String tmpContent = content.substring(start, start + contentLength/2);
      LCObject ticket = new LCObject("Ticket");
      ticket.put("content", tmpContent);
      ticket.put("author", author + i);
      ticket.save();
    }
  }

  public void testGenericQuery() throws Exception {
    List<String> fields = new ArrayList<>();
    fields.add("content");
    fields.add("author");
    String queryString = "推送";
    LCSearchQuery searchQuery = new LCSearchQuery(queryString);
    searchQuery.setClassName("Ticket");
    searchQuery.setSkip(0);
    searchQuery.setLimit(20);
    searchQuery.setFields(fields);
    searchQuery.addAscendingOrder("updatedAt");
    searchQuery.addDescendingOrder("createdAt");

    assertEquals(queryString, searchQuery.getQueryString());
    assertEquals(0, searchQuery.getSkip());
    assertEquals(20, searchQuery.getLimit());
    assertEquals(true, StringUtil.isEmpty(searchQuery.getHightLights()));

    searchQuery.findInBackground().subscribe(new Observer<List<LCObject>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<LCObject> results) {
        testSucceed = results.size() > 10;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

}
