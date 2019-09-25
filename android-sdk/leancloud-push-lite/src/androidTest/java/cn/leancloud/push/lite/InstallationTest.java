package cn.leancloud.push.lite;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class InstallationTest {
  private String appId = "testAppId-Gzshfie";
  private String appKey = "thisi s a test appkey....!!";
  private Context appContext = null;

  @Before
  public void setUp() {
    appContext = InstrumentationRegistry.getTargetContext();
    AVOSCloud.initialize(appContext, appId, appKey);
  }

  @Test
  public void testReadWriteLocalFile() throws Exception {
    assertEquals(true, null != appContext);
  }
}
