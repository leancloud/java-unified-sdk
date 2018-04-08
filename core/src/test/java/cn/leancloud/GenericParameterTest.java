package cn.leancloud;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public class GenericParameterTest extends TestCase {
  final static class __<T> {
    public T obj;
    //private Class<T> typeOfT;

    private __(T value) {
      this.obj = value;
      System.out.println("Class of This: " + this.getClass());
      System.out.println("Class of genericSuper: " + this.getClass().getGenericSuperclass());
//      typeOfT = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    Class getTypeOfT() {
      try {
        Field[] fields = this.getClass().getFields();
        for (Field f : fields) {
          System.out.println(f.toString());
        }

        return this.getClass().getField("obj").getClass();
      } catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
      // return typeOfT;
    }
  }

  private <T> T getValue(String key, T defaultValue) {
    __<T> ins = new __<T>(defaultValue);
    System.out.println(ins.getClass());
    Class clazz = ins.getTypeOfT();
    System.out.println(clazz);
    if (Boolean.class.isAssignableFrom(clazz)) {
      System.out.println("Boolean type of parameter T");
      return (T) Boolean.valueOf(true);
    } else if (Integer.class.isAssignableFrom(clazz)) {
      System.out.println("Integer type of parameter T");
      return (T) Integer.valueOf(110);
    } else if (Float.class.isAssignableFrom(clazz)) {
      System.out.println("Float type of parameter T");
      return (T) Float.valueOf(3.14f);
    } else if (Long.class.isAssignableFrom(clazz)) {
      System.out.println("Long type of parameter T");
      return (T) Long.valueOf(112434243);
    } else if (String.class.isAssignableFrom(clazz)) {
      System.out.println("String type of parameter T");
      return (T) "yeah";
    } else {
      System.out.println("unkown type of parameter T");
    }
    return null;
  }

  public GenericParameterTest(String name) {
    super(name);
  }
  public static Test suite() {
    return new TestSuite(GenericParameterTest.class);
  }

  public void testTypeInference() {
    String s = getValue("", "abc");
    Integer i = getValue("", 0);
    Long l = getValue("", 0l);
    Float f = getValue("", null);
    Boolean b = getValue(null, null);
    System.out.println("!!!Java doesnot support type inference in runtime!!!");
  }
}
