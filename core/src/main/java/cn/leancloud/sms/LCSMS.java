package cn.leancloud.sms;

import cn.leancloud.core.PaasClient;
import cn.leancloud.types.LCNull;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;

import java.util.Map;
import java.util.regex.Pattern;

public class LCSMS {
  static Pattern phoneNumPattern = Pattern.compile("^[1+]\\d+$");

  public enum TYPE {
    VOICE_SMS("voice"), TEXT_SMS("sms");

    private String name;

    TYPE(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }
  }

  public static boolean checkMobilePhoneNumber(String phoneNumber) {
    return phoneNumPattern.matcher(phoneNumber).find();
  }

  public static Observable<LCNull> requestSMSCodeInBackground(String mobilePhone, LCSMSOption option) {
    if (StringUtil.isEmpty(mobilePhone) || !checkMobilePhoneNumber(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("mobile phone number is empty or invalid"));
    }
    if (null == option) {
      return Observable.error(new IllegalArgumentException("smsOption is null"));
    }
    Map<String, Object> param = option.getOptionMap();
    return PaasClient.getStorageClient().requestSMSCode(mobilePhone, param);
  }

  public static Observable<LCNull> verifySMSCodeInBackground(String code, String mobilePhone) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
      return Observable.error(new IllegalArgumentException("code or mobilePhone is empty"));
    }
    return PaasClient.getStorageClient().verifySMSCode(code, mobilePhone);
  }
}
