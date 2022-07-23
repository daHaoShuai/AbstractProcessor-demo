package com.da.web.function;

import com.da.web.core.Context;

/**
 * Author Da
 * Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * Date: 2022-05-31
 * Time: 19:31
 * 为了辨别路由组件和消费上下文,所以不用Consumer而是自己定义
 */
@FunctionalInterface
public interface Handler {
    void callback(Context ctx);
}
