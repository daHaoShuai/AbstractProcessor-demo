package com.da.orm.function;

import java.lang.reflect.Method;

/**
 * @Author Da
 * @Description:
 * @Date: 2022-07-22
 * @Time: 16:34
 * 在代理对象前的操作
 */
public interface ProxyBefore {
    void exec(Method method, Object[] args);
}
