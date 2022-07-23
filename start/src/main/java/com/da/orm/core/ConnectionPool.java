package com.da.orm.core;

import java.sql.Connection;

/**
 * @Author Da
 * @Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * @Date: 2022-06-25
 * @Time: 15:51
 * 连接池接口
 */
public interface ConnectionPool {
    //    获取连接
    Connection getConnection();

    //    释放连接
    void releaseConnection(Connection connection);
}
