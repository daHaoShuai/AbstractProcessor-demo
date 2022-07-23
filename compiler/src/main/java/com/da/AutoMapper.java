package com.da;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author Da
 * @Description:
 * @Date: 2022-07-23
 * @Time: 14:27
 * 用来在编译的时候生成实体类对应的接口
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoMapper {
}
