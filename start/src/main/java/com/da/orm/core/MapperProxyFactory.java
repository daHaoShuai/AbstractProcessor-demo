package com.da.orm.core;

import com.da.orm.annotation.Delete;
import com.da.orm.annotation.Insert;
import com.da.orm.annotation.Select;
import com.da.orm.annotation.Update;
import com.da.orm.function.ProxyBefore;
import com.da.orm.utils.DBUtil;
import com.da.orm.utils.StringUtil;
import com.da.orm.utils.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @Author Da
 * @Description:
 * @Date: 2022-07-22
 * @Time: 10:27
 * mapper工厂
 */
public class MapperProxyFactory {

    private static final DBUtil dbUtil = DBUtil.getInstance();
    private static final Connection connection = dbUtil.getConnection();
    public static ProxyBefore before;

    //    返回代理后的mapper
    @SuppressWarnings("unchecked")//忽略强转类型的警告
    public static <T> T getMapper(Class<T> mapper) {
//        创建代理对象
        final Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{mapper}, (proxy, method, args) -> {
//                    在这个方法执行前的操作
                    if (before != null) {
                        before.exec(method, args);
                    }
//             处理 @Select 或者 @Insert 或者 @Update 或者 @Delete 注解的方法
                    if (method.isAnnotationPresent(Select.class)) {
                        return handlerSelect(method, args);
                    } else if (method.isAnnotationPresent(Insert.class)) {
                        return handlerInsert(method, args);
                    } else if (method.isAnnotationPresent(Update.class)) {
                        return handlerUpdate(method, args);
                    } else if (method.isAnnotationPresent(Delete.class)) {
                        return handlerDelete(method, args);
                    } else {
                        return null;
                    }
                });
        return (T) proxyInstance;
    }

    //    处理@Select注解的方法
    private static <T> Object handlerSelect(Method method, Object[] args) throws SQLException {
//        拿到注解中的语句
        String sql = method.getAnnotation(Select.class).value();
        final Matcher matcher = StringUtil.regMateSql(sql);
//        替换匹配到的地方为? 暂时先这样处理
        while (matcher.find()) {
            sql = sql.replace(matcher.group(0), "?");
        }
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = parseStatement(sql, args);
            final boolean execute = statement.execute();
            if (execute) {
//                    获取返回的类型
                final Class<T> resultType = Utils.getResultType(method);
//                    获取返回类型的所有set方法
                final Map<String, Method> setterMethodMap = Utils.getTypeSetMap(resultType);
//                   获取读取到的数据库中的内容
                resultSet = statement.getResultSet();
//                    获取表中字段的名字
                final List<String> columnList = new ArrayList<>();
                final ResultSetMetaData metaData = resultSet.getMetaData();
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    columnList.add(metaData.getColumnName(i + 1));
                }
                final List<T> resultList = new ArrayList<>();
//                    一行一行读取结果集的数据
                while (resultSet.next()) {
                    final T t = resultType.getConstructor().newInstance();
                    for (String colName : columnList) {
//                        如果有表的列名有_需要处理一下
                        final String key = StringUtil.convertToLineHump(colName);
                        final Method setMethod = setterMethodMap.get(key);
                        setMethod.invoke(t, resultSet.getObject(colName));
                    }
                    resultList.add(t);
                }
                if (resultList.size() > 1) {
                    return resultList;
                } else {
                    return resultList.get(0);
                }
            } else {
                throw new SQLException("sql语句执行失败,请检查sql语句是否正确");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
            dbUtil.closeConnection(connection);
        }
        return null;
    }

    //    处理@Insert注解的方法
    private static Object handlerInsert(Method method, Object[] args) throws SQLException {
        return execInsertOrUpdate(method.getAnnotation(Insert.class).value(), method, args);
    }

    //    处理@Update注解的方法
    private static Object handlerUpdate(Method method, Object[] args) throws SQLException {
        return execInsertOrUpdate(method.getAnnotation(Update.class).value(), method, args);
    }

    //    处理@Delete注解的方法
    private static Object handlerDelete(Method method, Object[] args) throws SQLException {
        String sql = method.getAnnotation(Delete.class).value();
        PreparedStatement statement = null;
        try {
            final Matcher matcher = StringUtil.regMateSql(sql);
//        暂时先替换成?
            while (matcher.find()) {
                sql = sql.replace(matcher.group(0), "?");
            }
            statement = parseStatement(sql, args);
            return !statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (statement != null) {
                statement.close();
            }
            dbUtil.closeConnection(connection);
        }
    }

    //    给sql语句的?填充对应的值
    private static PreparedStatement parseStatement(String sql, Object[] args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
        }
        return statement;
    }

    //    给sql语句的?填充对应的值
    private static PreparedStatement parseStatement(String sql, List<Object> args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        if (args != null && args.size() > 0) {
            for (int i = 0; i < args.size(); i++) {
                statement.setObject(i + 1, args.get(i));
            }
        }
        return statement;
    }

    @SuppressWarnings("unchecked")//忽略强转类型的警告
    private static <T> boolean execInsertOrUpdate(String sql, Method method, Object[] args) throws SQLException {
        PreparedStatement statement = null;
        try {
//        获取方法的输入类型
            final Type[] types = method.getGenericParameterTypes();
//        如果输入的参数只有一个并且类型是实体类类型
            if (types.length == 1 && types[0] instanceof Class) {
//                获取当前实体类的类型
                Class<T> clz = (Class<T>) types[0];
//                获取到当前实体类的所以get方法
                final Map<String, Method> getMap = Utils.getTypeGetMap(clz);
                final Matcher matcher = StringUtil.regMateSql(sql);
                final List<Object> objs = new ArrayList<>();
                while (matcher.find()) {
                    String name = matcher.group(0);
//                    替换当前位置为?
                    sql = sql.replace(name, "?");
                    name = name.substring(2, name.length() - 1);
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
//                    拿到传来类的对应的属性的值
                    final Object value = getMap.get(name).invoke(args[0]);
                    objs.add(value);
                }
                statement = parseStatement(sql, objs);
            }
//            多个参数就是直接向对应的?填充
            else {
                statement = parseStatement(sql, args);
            }
//            执行插入或者更新操作,返回操作结果
//            如果返回的第一个结果是resultSet对象时,返回true,如果其为更新计数或者不存在任何结果,则返回 false
            return !statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (statement != null) {
                statement.close();
            }
            dbUtil.closeConnection(connection);
        }
    }
}
