package com.da.orm.function;

/**
 * @Author Da
 * @Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * @Date: 2022-06-24
 * @Time: 9:59
 * 消费List和它对应的坐标
 */
@FunctionalInterface
public interface ConsumerListAndIndex<T> {
    void each(T t, int i);
}
