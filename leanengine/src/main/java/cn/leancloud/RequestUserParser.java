package cn.leancloud;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.leancloud.core.PaasClient;
import cn.leancloud.utils.LogUtil;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 用于解析 header 中间传递的请求用户信息
 * 
 * @author lbt05
 *
 * This class is deprecated since 7.2.1
 */
@Deprecated
class RequestUserParser {
  private static AVLogger LOGGER = LogUtil.getLogger(RequestUserParser.class);

  public static void parse(final HttpServletRequest req) {
    if (req.getAttribute(EngineRequestContext.ATTRIBUTE_KEY_AUTHENTICATION) == null) {
      return;
    }
    String sessionToken =
        ((PlatformRequestAuthentication) req.getAttribute(EngineRequestContext.ATTRIBUTE_KEY_AUTHENTICATION)).getSessionToken();
    if (sessionToken != null && !StringUtil.isEmpty(sessionToken)) {
      Map<String, String> header = new HashMap<String, String>();
      header.put("X-LC-Session", sessionToken);
      PaasClient.getStorageClient().createUserBySession(sessionToken, AVUser.class)
              .subscribe(new Observer<AVUser>() {
                public void onSubscribe(Disposable disposable) {

                }

                public void onNext(AVUser avUser) {
//                  AVUser.changeCurrentUser(avUser, true);
                  req.setAttribute(EngineRequestContext.ATTRIBUTE_KEY_SESSION_TOKEN, avUser);
                }

                public void onError(Throwable throwable) {
                  LOGGER.w(throwable);
                }

                public void onComplete() {

                }
              });
    }
  }
}
