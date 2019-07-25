package org.triski.faster.dao.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    boolean id() default false;

    boolean autoIncrement() default false;

    int length() default 40; // only varchar

    boolean unique() default false;

    String comment() default "";

    String defaultValue() default "";
}
