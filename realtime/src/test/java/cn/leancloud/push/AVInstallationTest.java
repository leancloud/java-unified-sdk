package cn.leancloud.push;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import junit.framework.TestCase;

public class AVInstallationTest extends TestCase {
  public AVInstallationTest(String testName) {
    super(testName);
    Configure.initialize();
  }

  public void testCreateInstallation() {
    AVInstallation install = new AVInstallation();
    assertTrue(install.getInstallationId().length() > 0);
    AVInstallation currentInstall = AVInstallation.getCurrentInstallation();
    assertTrue(currentInstall != null);
    assertTrue(install.getInstallationId().equals(currentInstall.getInstallationId()));
  }

  public void testSaveInstallation() {
    AVInstallation currentInstall = AVInstallation.getCurrentInstallation();
    currentInstall.saveInBackground().blockingFirst();
  }
  public void testSaveInstallationWithCustomProp() {
    AVInstallation currentInstall = AVInstallation.getCurrentInstallation();
    currentInstall.put("chan", "Chan");
    currentInstall.saveInBackground().blockingFirst();
  }
}
