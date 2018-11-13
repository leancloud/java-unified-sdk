package cn.leancloud.im.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AVIMMessageField {
  String name() default "";
}
