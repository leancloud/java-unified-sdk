package cn.leancloud.push.lite;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class InstallationTest {
  private String appId = "";
  private String appKey = "";

  public InstallationTest() {

  }

  @Test
  public void testGetterSetter() throws Exception {
    AVInstallation installation = new AVInstallation();
    installation.put("channel", Arrays.asList("public", "common"));
    List v = installation.getList("channel");
    assertTrue(null != v);
    assertTrue(v.size() == 2);

    assertTrue(null != installation.getString("deviceType"));
    assertTrue(null != installation.getString("timeZone"));
  }
}
