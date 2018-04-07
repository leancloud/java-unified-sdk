package cn.leancloud.sms;

import cn.leancloud.core.PaasClient;
import cn.leancloud.types.AVNull;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;

import java.util.Map;

public class AVSMS {
  public enum TYPE {
    VOICE_SMS("voice"), TEXT_SMS("text");

    private String name;

    TYPE(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }
  }

  public static Observable<AVNull> requestSMSCodeInBackground(String mobilePhone, AVSMSOption option) {
    if (StringUtil.isEmpty(mobilePhone)) {
      throw new IllegalArgumentException("mobile phone number is empty");
    }
    if (null == option) {
      throw new IllegalArgumentException("smsOption is null");
    }
    Map<String, Object> param = option.getOptionMap();
    return PaasClient.getStorageClient().requestSMSCode(mobilePhone, param);
  }

  public static Observable<AVNull> verifySMSCodeInBackground(String code, String mobilePhone) {
    if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
      throw new IllegalArgumentException("code or mobilePhone is empty");
    }
    return PaasClient.getStorageClient().verifySMSCode(code, mobilePhone);
  }
}
