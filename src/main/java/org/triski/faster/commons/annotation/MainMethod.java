package org.triski.faster.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author triski
 * @date 2019/7/14
 * @describe 主方法（被其它方法依赖的方法）
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MainMethod {
}
