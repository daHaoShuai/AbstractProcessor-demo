package com.da.orm.core;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
 * @Time: 15:53
 * 连接池实现类
 * <p>
 * 通过getConnection()，获取连接，如果空闲线程大于0，则直接从空闲线程取连接,此时freeConnectPool计数减1，activeConnectPool计数加1;
 * 否则需要判断当前线程连接是否达到最大连接数，如果达到，则wait()之后，继续调用getConnection(),
 * 如果当前线程连接未达到最大连接数，则直接新建一个连接，并放入activeConnectPool。
 * 通过releaseConnection(),释放当前连接。如果freeConnectPool已满，证明空闲线程足够多，直接关闭此连接；
 */
public class ConnectionPoolImpl implements ConnectionPool {

    //    空闲线程池
    private final List<Connection> freeConnectPool = new CopyOnWriteArrayList<>();
    //    活动线程池
    private final List<Connection> activeConnectPool = new CopyOnWriteArrayList<>();
    //    数据库连接配置
    private final DBConfig config;
    //    计算最大连接数
    int count = 0;

    public ConnectionPoolImpl(DBConfig config) {
        this.config = config;
//        初始化连接池
        initPool();
    }

    @Override
    public synchronized Connection getConnection() {

        //空闲线程存在空闲连接
        Connection connection;
        if (freeConnectPool.size() > 0) {
            connection = freeConnectPool.remove(0);
            activeConnectPool.add(connection);
        } else {
            //判断当前线程连接数量是否达到最大值
            if (count < config.getMaxActiveConnections()) {
                connection = createConnection();
                activeConnectPool.add(connection);
                count++;
            } else {
                try {
                    //活动线程已满等待解封
                    wait(config.getConnTimeOut());
                    //递归调用
                    connection = getConnection();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return connection;
    }

    @Override
    public synchronized void releaseConnection(Connection connection) {
        //如果空闲线程数已满，证明连接足够多
        if (freeConnectPool.size() < config.getMaxConnections()) {
            freeConnectPool.add(connection);
        } else {
            try {
                //此时维持线程只有10个
                connection.close();
                activeConnectPool.remove(connection);
                count--;
                notifyAll();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //        初始化连接池
    private void initPool() {
        if (config == null) {
            throw new RuntimeException("配置为空");
        }
        for (int i = 0; i < config.getInitConnections(); i++) {
            //新建连接
            Connection connection = createConnection();
            if (null != connection) {
//                加到空闲池中
                freeConnectPool.add(connection);
                count++;
            }
        }
    }

    //    创建与数据库的连接
    private Connection createConnection() {
        Driver driver;
        Connection connection = null;
        try {
            driver = (Driver) Class.forName(config.getDriver()).newInstance();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
