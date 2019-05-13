package cn.leancloud.push.lite;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.lang.reflect.Type;

public class AVObjectSerializer implements ObjectSerializer {
  public static final AVObjectSerializer instance = new AVObjectSerializer();

  @Override
  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType)
      throws IOException {
    SerializeWriter out = serializer.getWriter();
    AVInstallation avObject = (AVInstallation) object;
    out.write('{');
//    out.writeFieldValue(' ', "@type", avObject.getClass().getName());
//    out.writeFieldValue(',', "objectId", avObject.getObjectId());
//    out.writeFieldValue(',', "updatedAt", AVUtils.getAVObjectUpdatedAt(avObject));
//    out.writeFieldValue(',', "createdAt", AVUtils.getAVObjectCreatedAt(avObject));
//    String className = AVUtils.getAVObjectClassName(avObject.getClass());
//    out.writeFieldValue(',', "className", className == null ? avObject.getClassName() : className);
//    out.write(',');
//
//    out.writeFieldName("serverData");
    out.write(JSON.toJSONString(avObject.serverData, ObjectValueFilter.instance,
        SerializerFeature.WriteClassName,
        SerializerFeature.DisableCircularReferenceDetect));
    out.write('}');
  }

}
