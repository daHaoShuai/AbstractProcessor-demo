package com.da.orm.core;

import java.util.List;

/**
 * @Author Da
 * @Description:
 * @Date: 2022-07-23
 * @Time: 14:13
 * 用来给编译器动态生成class文件
 */
public interface BaseMapper<T> {

    List<T> list();

    T getById(Integer id);

    boolean delete(Integer id);
}
