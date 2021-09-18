package cn.leancloud.upload;

import cn.leancloud.LCException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

public class FileDownloaderTest extends TestCase {
    public FileDownloaderTest(String testName) {
        super(testName);
    }
    public static Test suite() {
        return new TestSuite(FileDownloaderTest.class);
    }

    public void testDownloadExternalFile() {
        File localFile = new File("./file" + System.currentTimeMillis());
        FileDownloader downloader = new FileDownloader();
        String url = "https://img2.tapimg.com/bbcode/images/1a667685a3d219cfd780ee3f0592a067.png";
        LCException exception = downloader.execute(url, localFile);
        assertTrue(null == exception);
        localFile.delete();
    }

    public void testDownloadQiniuFile() {
        File localFile = new File("./file" + System.currentTimeMillis());
        FileDownloader downloader = new FileDownloader();
        String url = "https://file.leanticket.cn/O5l1x5WbFPKyKPEFM8YK705twvGapA3n/image.png";
        LCException exception = downloader.execute(url, localFile);
        assertTrue(null == exception);
        localFile.delete();
    }

    public void testDownloadForbiddenFile() {
        File localFile = new File("./file" + System.currentTimeMillis());
        FileDownloader downloader = new FileDownloader();
        String url = "http://lc-nw1kssnv.cn-n1.lcfile.com/GP95OXcuxxQhOKVTjeg5RWRs145sjwCDH2qiMWeJ.jpg";
        LCException exception = downloader.execute(url, localFile);
        assertTrue(null != exception);
        localFile.delete();
    }

}
