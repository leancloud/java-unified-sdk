package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.utils.LogUtil;
import com.alibaba.fastjson.JSON;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AVObjectSerializer2Test extends TestCase {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVObjectSerializer2Test.class);
  private static final String CLASSNAME_STUDENT = "Student";
  private static final String FILE_OBJECT_ID = "5bff45249f54540066d4d829";
  private String studentId = null;
  public AVObjectSerializer2Test(String name) {
    super(name);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(AVObjectSerializer2Test.class);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testComplexObjectDescerializer() {
    AVQuery q = new AVQuery("Student");
    q.orderByDescending("createdAt");
    AVObject o = q.getFirst();
    System.out.println(o.toJSONString());
    String arhiveString = ArchivedRequests.getArchiveContent(o, false);
    System.out.println(arhiveString);
    AVObject oldV = ArchivedRequests.parseAVObject(arhiveString);
    System.out.println(oldV.toJSONString());
  }

}
