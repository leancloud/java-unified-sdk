package cn.leancloud;

import io.reactivex.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AVAnonymousUtils {
  private AVAnonymousUtils() {}

  private static Map<String, Object> anonymousAuthData() {
    Map<String, Object> idData = new HashMap<>();
    idData.put("id", UUID.randomUUID().toString().toLowerCase());
    return idData;
  }

  public static Observable<AVUser> logIn() {
    Map<String, Object> authData = anonymousAuthData();
    return AVUser.loginWithAuthData(authData, "anonymous");
  }

}
