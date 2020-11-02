package cn.leancloud.upload;

import cn.leancloud.json.JSON;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FileUploadTokenTest extends TestCase {
  public FileUploadTokenTest(String testName) {
    super(testName);
  }
  public static Test suite() {
    return new TestSuite(FileUploadTokenTest.class);
  }

  public void testJSONObject() {
    FileUploadToken token = new FileUploadToken();
    token.setBucket("bucketV");
    token.setObjectId("objectIdV");
    token.setProvider("providerV");
    token.setToken("tokenV");
    token.setUploadUrl("uploadUrlV");
    token.setUrl("http://upload.com/file");
    token.setKey("dxStIHG3RcqvA6BK5DDJqQCsouDE1IgF/test.jpeg");
    String str = JSON.toJSONString(token);
    FileUploadToken token2 = JSON.parseObject(str, FileUploadToken.class);
    assert token.equals(token2);
  }
}
