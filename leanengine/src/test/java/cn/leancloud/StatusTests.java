package cn.leancloud;

import cn.leancloud.core.AppConfiguration;
import cn.leancloud.core.LeanCloud;
import cn.leancloud.logging.Log4jAdapter;
import junit.framework.TestCase;

import java.util.List;

public class StatusTests extends TestCase {
    public StatusTests(String name) {
        super(name);
        System.setProperty(EngineAppConfiguration.SYSTEM_ATTR_APP_PORT, "3000");
        AppConfiguration.setLogAdapter(new Log4jAdapter());
        LeanCloud.setRegion(LeanCloud.REGION.NorthChina);
        LeanCloud.setLogLevel(LCLogger.Level.VERBOSE);
        LeanEngine.initialize("-gzGzoHsz", "",
                "", null);
        LeanEngine.setUseMasterKey(true);
    }

    public void testGetStatus() throws Exception {
        String sessionToken = "82";
        LCUser avUser = LCUser.becomeWithSessionToken(sessionToken);
        System.out.println("getStatusWithImage getLoginUser" + avUser.toString());
        String defaultInbox = LCStatus.INBOX_TYPE.TIMELINE.toString(); // "default"
        LCStatusQuery statusQuery = LCStatus.inboxQuery(avUser, defaultInbox);
        statusQuery.skip(0);
        statusQuery.limit(20);
        List<LCStatus> statusList = statusQuery.find();
        for (LCStatus st: statusList) {
            System.out.println(st.toJSONString());
        }
        assertTrue(statusList.size() > 0);
    }
}
