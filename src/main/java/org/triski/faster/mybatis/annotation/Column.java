package org.triski.faster.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    boolean id() default false;

    /* 当 id 为 true 时有效， 默认自增 */
    boolean autoIncrement() default true;

    /* length 只对 String 有作用 */
    int length() default 40;

    boolean unique() default false;

    String comment() default "";

    /* 数值型参数默认为 0； 字符型参数默认为 ‘’ */
    String defaultValue() default "";

}
