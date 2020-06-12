package cn.leancloud.json;

import cn.leancloud.*;
import cn.leancloud.ops.BaseOperation;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;

public class ConverterUtils {

  public static void initialize() {
    ObjectTypeAdapter adapter = new ObjectTypeAdapter();
    ParserConfig.getGlobalInstance().putDeserializer(AVObject.class, adapter);
    ParserConfig.getGlobalInstance().putDeserializer(AVUser.class, adapter);
    ParserConfig.getGlobalInstance().putDeserializer(AVFile.class, adapter);
    ParserConfig.getGlobalInstance().putDeserializer(AVRole.class, adapter);
    ParserConfig.getGlobalInstance().putDeserializer(AVStatus.class, adapter);
    ParserConfig.getGlobalInstance().putDeserializer(AVInstallation.class, adapter);

    SerializeConfig.getGlobalInstance().put(AVObject.class, adapter);
    SerializeConfig.getGlobalInstance().put(AVUser.class, adapter);
    SerializeConfig.getGlobalInstance().put(AVFile.class, adapter);
    SerializeConfig.getGlobalInstance().put(AVRole.class, adapter);
    SerializeConfig.getGlobalInstance().put(AVStatus.class, adapter);
    SerializeConfig.getGlobalInstance().put(AVInstallation.class, adapter);

    BaseOperationAdapter opAdapter = new BaseOperationAdapter();
    ParserConfig.getGlobalInstance().putDeserializer(BaseOperation.class, opAdapter);
    SerializeConfig.getGlobalInstance().put(BaseOperation.class, opAdapter);
  }

  public static <T extends AVObject> void registerClass(Class<T> clazz) {
    ParserConfig.getGlobalInstance().putDeserializer(clazz, new ObjectTypeAdapter());
    SerializeConfig.getGlobalInstance().put(clazz, new ObjectTypeAdapter());
  }
}
