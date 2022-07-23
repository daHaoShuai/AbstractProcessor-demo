package com.da.orm.utils;

import com.da.orm.core.ConnectionPoolImpl;
import com.da.orm.core.DBConfig;

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
 * @Date: 2022-06-23
 * @Time: 9:23
 * 数据库连接工具类
 */
public class DBUtil {

    private static DBUtil instance = null;

    //    连接池
    private ConnectionPoolImpl connectionPool;

    private DBUtil() {
        initConnect();
    }

    //    单例模式,只初始化连接一次数据库
    public static DBUtil getInstance() {
        if (null == instance) {
            instance = new DBUtil();
        }
        return instance;
    }

    //    数据库连接配置信息
    private void initConnect() {
        DBConfig config = new DBConfig();
        connectionPool = new ConnectionPoolImpl(config);
    }

    //    获取连接
    public Connection getConnection() {
        return connectionPool.getConnection();
    }

    //    关闭连接
    public void closeConnection(Connection connection) {
        connectionPool.releaseConnection(connection);
    }
}
