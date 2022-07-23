package com.da.orm.core;

import java.util.List;

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
 * @Time: 16:59
 * 通用的增删改查接口
 */
public interface BaseCrud<T> {
    //    增加操作
    boolean add(T t);

    //    获取当前表的所有内容
    List<T> list();

    //    分页查询
    List<T> pages(int current, int pageSize);

    //    通过主键获取内容
    <O> T getById(O id);

    //    通过主键来删除
    <O> boolean deleteById(O id);

    //    通过主键来更新内容
    boolean updateById(T t);

    //    通过自定以的sql语句查询
    List<T> query(String sql);

    //    通过sql语句执行增删改操作
    boolean exec(String sql);

    //    关闭连接
    void closeConnection();
}
