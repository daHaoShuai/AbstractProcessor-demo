package com.da.orm.core;

import com.da.orm.utils.StringUtil;

import java.io.InputStream;
import java.util.Properties;

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
 * @Time: 15:35
 * 数据库连接配置
 */
public class DBConfig {
    private String username;
    private String password;
    private String url;
    private String driver;
    // 连接池名字
    private String poolName = "da_diy_mysql_pool";
    // 空闲池，最小连接数
    private int minConnections = 1;
    // 空闲池，最大连接数
    private int maxConnections = 10;
    // 初始化连接数
    private int initConnections = 5;
    // 重复获得连接的频率
    private int connTimeOut = 1000;
    // 最大允许的连接数，和数据库对应
    private int maxActiveConnections = 100;
    // 连接超时时间，默认20分钟
    private int connectionTimeOut = 1000 * 60 * 20;

    public DBConfig() {
        initConfig();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public int getMinConnections() {
        return minConnections;
    }

    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getInitConnections() {
        return initConnections;
    }

    public void setInitConnections(int initConnections) {
        this.initConnections = initConnections;
    }

    public int getConnTimeOut() {
        return connTimeOut;
    }

    public void setConnTimeOut(int connTimeOut) {
        this.connTimeOut = connTimeOut;
    }

    public int getMaxActiveConnections() {
        return maxActiveConnections;
    }

    public void setMaxActiveConnections(int maxActiveConnections) {
        this.maxActiveConnections = maxActiveConnections;
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    //    读取配置信息
    private void initConfig() {
        try {
            final InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple-orm.properties");
            if (null == is) {
                throw new RuntimeException("没有找到配置文件,请检查配置文件 simple-orm.properties 是否在 resources 目录下");
            } else {
                final Properties properties = new Properties();
                properties.load(is);
                this.username = properties.getProperty("username");
                this.password = properties.getProperty("password");
                this.url = properties.getProperty("url");
                this.driver = properties.getProperty("driver");
//                如果配置文件中有就覆盖默认的
                final String poolName = properties.getProperty("poolName");
                if (!StringUtil.isEmpty(poolName)) this.poolName = poolName;
                final String minConnections = properties.getProperty("minConnections");
                if (!StringUtil.isEmpty(minConnections)) this.minConnections = Integer.parseInt(minConnections);
                final String maxConnections = properties.getProperty("maxConnections");
                if (!StringUtil.isEmpty(maxConnections)) this.maxConnections = Integer.parseInt(maxConnections);
                final String initConnections = properties.getProperty("initConnections");
                if (!StringUtil.isEmpty(initConnections)) this.initConnections = Integer.parseInt(initConnections);
                final String connTimeOut = properties.getProperty("connTimeOut");
                if (!StringUtil.isEmpty(connTimeOut)) this.connTimeOut = Integer.parseInt(connTimeOut);
                final String maxActiveConnections = properties.getProperty("maxActiveConnections");
                if (!StringUtil.isEmpty(maxActiveConnections))
                    this.maxActiveConnections = Integer.parseInt(maxActiveConnections);
                final String connectionTimeOut = properties.getProperty("connectionTimeOut");
                if (!StringUtil.isEmpty(connectionTimeOut))
                    this.connectionTimeOut = Integer.parseInt(connectionTimeOut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
