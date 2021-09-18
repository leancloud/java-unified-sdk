package cn.leancloud.json;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TypeReference<T> {
  static ConcurrentMap<Type, Type> classTypeCache = new ConcurrentHashMap(16, 0.75F, 1);
  protected final Type type;

  protected TypeReference() {
    Type superClass = this.getClass().getGenericSuperclass();
    Type type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
    Type cachedType = (Type)classTypeCache.get(type);
    if (cachedType == null) {
      classTypeCache.putIfAbsent(type, type);
      cachedType = (Type)classTypeCache.get(type);
    }

    this.type = cachedType;
  }

  protected TypeReference(Type... actualTypeArguments) {
    Class<?> thisClass = this.getClass();
    Type superClass = thisClass.getGenericSuperclass();
    ParameterizedType argType = (ParameterizedType)((ParameterizedType)superClass).getActualTypeArguments()[0];
    Type rawType = argType.getRawType();
    Type[] argTypes = argType.getActualTypeArguments();
    int actualIndex = 0;

    for(int i = 0; i < argTypes.length; ++i) {
      if (argTypes[i] instanceof TypeVariable && actualIndex < actualTypeArguments.length) {
        argTypes[i] = actualTypeArguments[actualIndex++];
      }

      if (argTypes[i] instanceof GenericArrayType) {
        argTypes[i] = checkPrimitiveArray((GenericArrayType)argTypes[i]);
      }

      if (argTypes[i] instanceof ParameterizedType) {
        argTypes[i] = this.handlerParameterizedType((ParameterizedType)argTypes[i], actualTypeArguments, actualIndex);
      }
    }

    Type key = new ParameterizedTypeImpl(argTypes, thisClass, rawType);
    Type cachedType = classTypeCache.get(key);
    if (cachedType == null) {
      classTypeCache.put(key, key);
      cachedType = classTypeCache.get(key);
    }

    this.type = cachedType;
  }

  public static Type checkPrimitiveArray(GenericArrayType genericArrayType) {
    Type clz = genericArrayType;
    Type genericComponentType = genericArrayType.getGenericComponentType();

    String prefix;
    for(prefix = "["; genericComponentType instanceof GenericArrayType; prefix = prefix + prefix) {
      genericComponentType = ((GenericArrayType)genericComponentType).getGenericComponentType();
    }

    if (genericComponentType instanceof Class) {
      Class<?> ck = (Class)genericComponentType;
      if (ck.isPrimitive()) {
        try {
          if (ck == Boolean.TYPE) {
            clz = Class.forName(prefix + "Z");
          } else if (ck == Character.TYPE) {
            clz = Class.forName(prefix + "C");
          } else if (ck == Byte.TYPE) {
            clz = Class.forName(prefix + "B");
          } else if (ck == Short.TYPE) {
            clz = Class.forName(prefix + "S");
          } else if (ck == Integer.TYPE) {
            clz = Class.forName(prefix + "I");
          } else if (ck == Long.TYPE) {
            clz = Class.forName(prefix + "J");
          } else if (ck == Float.TYPE) {
            clz = Class.forName(prefix + "F");
          } else if (ck == Double.TYPE) {
            clz = Class.forName(prefix + "D");
          }
        } catch (ClassNotFoundException var6) {
        }
      }
    }

    return (Type)clz;
  }

  private Type handlerParameterizedType(ParameterizedType type, Type[] actualTypeArguments, int actualIndex) {
    Class<?> thisClass = this.getClass();
    Type rawType = type.getRawType();
    Type[] argTypes = type.getActualTypeArguments();

    for(int i = 0; i < argTypes.length; ++i) {
      if (argTypes[i] instanceof TypeVariable && actualIndex < actualTypeArguments.length) {
        argTypes[i] = actualTypeArguments[actualIndex++];
      }

      if (argTypes[i] instanceof GenericArrayType) {
        argTypes[i] = checkPrimitiveArray((GenericArrayType)argTypes[i]);
      }

      if (argTypes[i] instanceof ParameterizedType) {
        return this.handlerParameterizedType((ParameterizedType)argTypes[i], actualTypeArguments, actualIndex);
      }
    }

    Type key = new ParameterizedTypeImpl(argTypes, thisClass, rawType);
    return key;
  }

  public Type getType() {
    return this.type;
  }
}
