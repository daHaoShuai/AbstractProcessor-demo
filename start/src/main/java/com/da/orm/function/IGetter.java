package com.da.orm.function;

import java.io.Serializable;

/**
 * @Author Da
 * @Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * @Date: 2022-06-27
 * @Time: 17:21
 * 通过这个接口获取属性的名字
 */
@FunctionalInterface
public interface IGetter<T> extends Serializable {
    Object get(T source);
}
