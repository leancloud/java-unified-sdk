package cn.leancloud.json;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.lang.reflect.ParameterizedType;

public class ParameterizedTypeImpl implements ParameterizedType{
  private final Type[] actualTypeArguments;
  private final Type ownerType;
  private final Type rawType;

  public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
    this.actualTypeArguments = actualTypeArguments;
    this.ownerType = ownerType;
    this.rawType = rawType;
  }

  public Type[] getActualTypeArguments() {
    return this.actualTypeArguments;
  }

  public Type getOwnerType() {
    return this.ownerType;
  }

  public Type getRawType() {
    return this.rawType;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && this.getClass() == o.getClass()) {
      ParameterizedTypeImpl that = (ParameterizedTypeImpl)o;
      if (!Arrays.equals(this.actualTypeArguments, that.actualTypeArguments)) {
        return false;
      } else {
        if (this.ownerType != null) {
          if (this.ownerType.equals(that.ownerType)) {
            return this.rawType != null ? this.rawType.equals(that.rawType) : that.rawType == null;
          }
        } else if (that.ownerType == null) {
          return this.rawType != null ? this.rawType.equals(that.rawType) : that.rawType == null;
        }

        return false;
      }
    } else {
      return false;
    }
  }

  public int hashCode() {
    int result = this.actualTypeArguments != null ? Arrays.hashCode(this.actualTypeArguments) : 0;
    result = 31 * result + (this.ownerType != null ? this.ownerType.hashCode() : 0);
    result = 31 * result + (this.rawType != null ? this.rawType.hashCode() : 0);
    return result;
  }
}
