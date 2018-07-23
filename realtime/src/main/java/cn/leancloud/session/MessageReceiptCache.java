package cn.leancloud.session;

import cn.leancloud.core.AppConfiguration;
import cn.leancloud.utils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class MessageReceiptCache {
  private static final String MESSAGE_ZONE = "com.avoscloud.chat.receipt.";
  private static final String QUEUE_KEY = "com.avoscloud.chat.message.receipt";

  public static void add(String sessionId, String key, Object value) {
    String queueString =
            JSON.toJSONString(value, SerializerFeature.SkipTransientField,
                    SerializerFeature.WriteClassName, SerializerFeature.QuoteFieldNames,
                    // SerializerFeature.DisableCircularReferenceDetect,
                    SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullBooleanAsFalse);
    AppConfiguration.getDefaultSetting().saveString(MESSAGE_ZONE + sessionId,
            QUEUE_KEY + key,
            queueString);
  }

  public static Object get(String sessionId, String key) {
    String valueString =
            AppConfiguration.getDefaultSetting().getString(MESSAGE_ZONE + sessionId,
                    QUEUE_KEY + key, null);
    AppConfiguration.getDefaultSetting().removeKey(MESSAGE_ZONE, QUEUE_KEY + key);
    if (StringUtil.isEmpty(valueString)) {
      return null;
    }
    return JSON.parse(valueString);
  }

  public static void clean(String sessionId) {
    AppConfiguration.getDefaultSetting().removeKeyZone(MESSAGE_ZONE + sessionId);
  }

}
