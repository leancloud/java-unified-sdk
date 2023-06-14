package cn.leancloud;

import cn.leancloud.utils.LogUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LCObjectSerializer2Test extends TestCase {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCObjectSerializer2Test.class);
  private static final String CLASSNAME_STUDENT = "Student";
  private static final String FILE_OBJECT_ID = "5e12b15dd4b56c007747193d";
  private String studentId = null;
  public LCObjectSerializer2Test(String name) {
    super(name);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(LCObjectSerializer2Test.class);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testComplexObjectDescerializer() {
    LCQuery q = new LCQuery("Student");
    q.orderByDescending("createdAt");
    q.whereExists("portrait");
    LCObject o = q.getFirst();
    if (null == o) {
      return;
    }
//    if (o.getAVFile("portrait") == null) {
//      AVFile portrait = new AVFile("thumbnail", "https://tvax1.sinaimg.cn/crop.0.0.200.200.180/a8d43f7ely1fnxs86j4maj205k05k74f.jpg");
//      o.put("portrait", portrait);
//      o.save();
//    }
    System.out.println(o.toJSONString());
    LCObject newO = LCObject.parseLCObject(o.toJSONString());
    assertTrue(null != newO);
    assertTrue(null != newO.getLCFile("portrait"));

    String arhiveString = ArchivedRequests.getArchiveContent(o, false);
    System.out.println(arhiveString);
    LCObject newV = ArchivedRequests.parseAVObject(arhiveString);
    System.out.println(newV.toJSONString());
    assertTrue(null != newV);
    assertTrue(null != newV.getLCFile("portrait"));
  }

  public void testDeserializedWithOperationQueueData() throws Exception {
    String json = "{ \"@type\":\"com.avos.avoscloud.AVObject\",\"objectId\":\"5b4f1e0dfe88c200357fda80\",\"updatedAt\":\"2018-12-25T15:18:51.574Z\",\"createdAt\":\"2018-07-18T11:01:33.931Z\",\"className\":\"Caricature\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"update\":\"更新至201话\",\"read_url\":\"http://ac.qq.com/ComicView/index/id/624388/cid/3\",\"type\":\"\",\"des\":\"洪明轩-外号“雪豹”这个传说中的格斗高手竟然——遭遇校园冷暴力?!被逼无奈，只能转学的他决心隐藏自己的真正实力，结果转学第一天就惹到了学校老大？还被学校里的一群麻烦家伙缠上了，“雪豹”到底该如何抉择呢，要展示一下自己的真正实力吗？【授权/周四六更新】\",\"tag\":\"校园 爆笑\",\"views\":3.3E+8,\"source_name\":\"腾讯动漫\",\"order\":1891,\"author\":\"作者：KTOON / YUYU \",\"isValid\":\"1\",\"book_img_url\":\"https://manhua.qpic.cn/vertical/0/08_22_58_fb3925ac11acc5063f244ad2aab3aad7_1507474687604.jpg/420\",\"url\":\"http://ac.qq.com/Comic/comicInfo/id/624388\",\"name\":\"进击吧,雪豹\",\"category\":\"\"},\"operationQueue\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"lastUrl\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"lastUrl\",\"type\":\"Set\",\"values\":\"https://m.ac.qq.com/chapter/index/id/624388/cid/3\"}}}";
    LCObject obj = LCObject.parseLCObject(json);
    assertTrue(null != obj);

    json = "{ \"@type\":\"com.avos.avoscloud.AVObject\",\"objectId\":\"5b04d56a9f545400880c1e00\",\"updatedAt\":\"2018-05-30T03:33:51.183Z\",\"createdAt\":\"2018-05-23T02:43:54.804Z\",\"className\":\"EnglishWebsite\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"Order\":599,\"ad_filte\":\"class:app-guiding bottom-fixed no-close#class:app-guiding#class:down-app-mask\",\"Title\":\"腾讯动漫\",\"category\":\"caricature\",\"ImgUrl\":\"http://www.mzxbkj.com/images/cartoon/cartoon_001.png\",\"Url\":\"http://m.ac.qq.com/\",\"IsValid\":\"1\"}}";
    obj = LCObject.parseLCObject(json);
    assertTrue(null != obj);
  }

  public void testDeserializedWithOldVersionStatus() throws Exception {
    String json = "{\"@type\":\"com.avos.avoscloud.AVStatus\",\"objectId\":\"5b4f1e0dfe88c200357fda80\",\"updatedAt\":\"2018-12-25T15:18:51.574Z\",\"createdAt\":\"2018-07-18T11:01:33.931Z\",\"className\":\"_Status\",\"dataMap\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"update\":\"更新至201话\"},\"inboxType\":\"default\", \"messageId\":\"pp-guiding bottom-fixed no-close#c\",\"source\":\"\"}";
    LCStatus status = (LCStatus) LCObject.parseLCObject(json);
    assertTrue(null != status);
    System.out.println(status);
  }

  public void testDeserializAVObject() throws Exception {
    String json = "{ \"@type\":\"com.avos.avoscloud.AVInstallation\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"_Installation\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    LCInstallation installation = (LCInstallation) LCObject.parseLCObject(json);
    assertTrue(null != installation);
    assertTrue(installation.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    json = "{ \"@type\":\"com.avos.avoscloud.AVObject\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"Student\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    LCObject student = (LCObject) LCObject.parseLCObject(json);
    assertTrue(null != student);
    assertTrue(student.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    json = "{ \"@type\":\"com.avos.avoscloud.AVUser\",\"objectId\":\"5c282efc9f54540070f04e9a\",\"updatedAt\":\"2018-12-30T02:35:40.331Z\",\"createdAt\":\"2018-12-30T02:35:40.331Z\",\"className\":\"_User\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"emailVerified\":false,\"password\":\"qwertyuiop\",\"sessionToken\":\"bckq6t7gvvfns2t7eer9imxq1\",\"portrait\":{\"@type\":\"com.avos.avoscloud.AVFile\",\"dataAvailable\":true,\"dirty\":false,\"metaData\":{\"@type\":\"java.util.HashMap\",\"_name\":\"thumbnail\",\"__source\":\"external\"},\"name\":\"5c282efc808ca4565c079bc6\",\"objectId\":\"5c282efc808ca4565c079bc6\",\"originalName\":\"thumbnail\",\"size\":-1,\"url\":\"https://tvax1.sinaimg.cn/crop.0.0.200.200.180/a8d43f7ely1fnxs86j4maj205k05k74f.jpg\"},\"mobilePhoneVerified\":false,\"username\":\"er@a.com\"}}";
    LCUser user = (LCUser) LCObject.parseLCObject(json);
    assertTrue(null != user);
    assertTrue(user.getObjectId().equals("5c282efc9f54540070f04e9a"));
    LCFile userProfile = user.getLCFile("portrait");
    assertTrue(null != userProfile);

    json = "{ \"@type\":  \"com.avos.avoscloud.AVStatus\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"_Status\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    LCStatus status = (LCStatus) LCObject.parseLCObject(json);
    assertTrue(null != status);
    assertTrue(status.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    json = "{ \"@type\":\"com.avos.avoscloud.AVFile\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"_File\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    LCFile file = (LCFile) LCObject.parseLCObject(json);
    assertTrue(null != file);
    assertTrue(file.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    try {
      json = "{ \"@type\":\"com.avos.avoscloud.AVPushOOO\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"Push\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
      LCObject o = (LCObject) LCObject.parseLCObject(json);
      System.out.println(o);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void testDeserializeDeepObject() throws Exception {
    String json = "{\"updatedAt\":\"2019-09-05T08:32:31.200Z\",\"createdAt\":\"2019-09-05T08:32:31.200Z\",\"taskId\":{\"updatedAt\":\"2019-09-05T08:14:29.183Z\",\"createdAt\":\"2019-09-05T08:14:29.183Z\",\"userId\":{\"updatedAt\":\"2019-06-18T08:28:23.812Z\",\"username\":\"100364\",\"createdAt\":\"2019-06-18T08:28:23.618Z\",\"nickName\":\"\\u5b66\\u5458100364\",\"objectId\":\"5d08a0a712215f00718cbc71\",\"__type\":\"Pointer\",\"className\":\"_User\"},\"objectId\":\"5d70c3e5c8959c0074f13310\",\"__type\":\"Pointer\",\"className\":\"hb_Task\"},\"objectId\":\"5d70c81f17b54d00680ddba3\"}";
    LCObject object = LCObject.parseLCObject(json);
    System.out.println("object=" + object);
    LCObject taskId = object.getLCObject("taskId");
    System.out.println("task=" + taskId);
    LCUser user = taskId.getLCObject("userId");
    assertTrue(null != user);
  }

  public void testUnsavedObject() throws Exception {
    LCObject todo = new LCObject("Todo"); // 构建对象
    todo.put("title", "马拉松报名"); // 设置名称
    todo.put("priority", 2); // 设置优先级
    todo.put("owner", LCUser.getCurrentUser()); // 这里就是一个 Pointer 类型，指向当前登录的用户
    String serializedString = todo.toString();
    System.out.println(serializedString);

    LCObject deserializedObject = LCObject.parseLCObject(serializedString);
    System.out.println(deserializedObject);

    serializedString = ArchivedRequests.getArchiveContent(todo, false);
    System.out.println(serializedString);
    deserializedObject = LCObject.parseLCObject(serializedString);
    System.out.println(deserializedObject.getString("title"));
    deserializedObject = ArchivedRequests.parseAVObject(serializedString);
    System.out.println(deserializedObject.getString("title"));
  }

  public void testArchivedObject() throws Exception {
    String content = "{ \"@type\":\"com.avos.avoscloud.AVObject\",\"objectId\":\"\",\"updatedAt\":null,\"createdAt\":null,\"className\":\"search_item\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\"},\"operationQueue\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"taobao_title\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"taobao_title\",\"type\":\"Set\",\"values\":\"海峡红茶叶小青柑茶柑普茶桔普茶独立包装礼盒装12颗/盒*2盒\"},\"image\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"image\",\"type\":\"Set\",\"values\":\"https://img.alicdn.com/bao/uploaded/i1/3527879058/O1CN01I75nAf2GmcE1GoBoQ_!!0-item_pic.jpg\"},\"taobao_cid\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"taobao_cid\",\"type\":\"Set\",\"values\":\"124458005\"},\"quan_end_time\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"quan_end_time\",\"type\":\"Set\",\"values\":1678291199000},\"item_id\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"item_id\",\"type\":\"Set\",\"values\":\"9GGBak3hBtZNyQWSnn4SQt6-OwJxwkcxnQe7rvMt9\"},\"discount_price\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"discount_price\",\"type\":\"Set\",\"values\":128.0D},\"tkRate\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"tkRate\",\"type\":\"Set\",\"values\":20.03D},\"title\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"title\",\"type\":\"Set\",\"values\":\"海峡红茶叶小青柑茶柑普茶桔普茶独立包装礼盒装12颗/盒*2盒\"},\"shop_name\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"shop_name\",\"type\":\"Set\",\"values\":\"海峡红旗舰店\"},\"shop_id\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"shop_id\",\"type\":\"Set\",\"values\":\"117095248441659929\"},\"taobao_pics\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"taobao_pics\",\"type\":\"Set\",\"values\":\"https://img.alicdn.com/bao/uploaded/i1/3527879058/O1CN01I75nAf2GmcE1GoBoQ_!!0-item_pic.jpg,https://img.alicdn.com/i3/3527879058/O1CN014aWQrh2GmcDtHeXL5_!!3527879058.jpg,https://img.alicdn.com/i3/3527879058/O1CN01iL8XHm2GmcDvFVNBE_!!3527879058.jpg,https://img.alicdn.com/i3/3527879058/O1CN01Cval6h2GmcE1nUJCT_!!3527879058.jpg,https://img.alicdn.com/i3/3527879058/O1CN01SvXMeS2GmcDwUlDzs_!!3527879058.jpg\"},\"shop_icon\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"shop_icon\",\"type\":\"Set\",\"values\":\"http://logo.taobaocdn.com/shop-logo/3d/7c/TB1xQ9clL6H8KJjSspmSuv2WXXa.jpg\"},\"price\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"price\",\"type\":\"Set\",\"values\":198.0D},\"shop_url\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"shop_url\",\"type\":\"Set\",\"values\":\"http://shop507219890.taobao.com\"},\"quan_price\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"quan_price\",\"type\":\"Set\",\"values\":70},\"item_url\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"item_url\",\"type\":\"Set\",\"values\":\"https://detail.tmall.com/item.htm?id=X42a345iGt3r6pkFmmOSBtg-OwJxwkcxnQe7rvMt9\"},\"shop_type\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"shop_type\",\"type\":\"Set\",\"values\":\"B\"},\"sell_num\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"sell_num\",\"type\":\"Set\",\"values\":1},\"item_id2\":{\"@type\":\"com.avos.avoscloud.ops.SetOp\",\"key\":\"item_id2\",\"type\":\"Set\",\"values\":\"X42a345iGt3r6pkFmmOSBtg-OwJxwkcxnQe7rvMt9\"}}}";
    LCObject deserializedObject = LCObject.parseLCObject(content);
    System.out.println(deserializedObject);
    deserializedObject = ArchivedRequests.parseAVObject(content);
    System.out.println(deserializedObject);
  }
}
