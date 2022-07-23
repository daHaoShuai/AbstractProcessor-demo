package com.da.orm.core;

import com.da.orm.utils.DBUtil;
import com.da.orm.utils.StringUtil;
import com.da.orm.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
 * @Time: 11:54
 * 基础的增删改查
 */
public class BaseDao<T> implements BaseCrud<T> {
    //    实例化数据库连接工具类
    private final DBUtil dbUtil;
    //    获取数据库连接
    private final Connection connection;
    //    sql语句构建
    private final Sql sqlBuild;
    //    对应的实体类类型
    private final Class<T> po;
    //    当前实体类上所有的属性
    private final List<Field> allField;
    //    对应表的字段名字
    private final List<String> allFieldName;
    //    主键属性
    private final Field primaryKey;
    //    主键名字
    private final String primaryKeyName;

    //    初始化信息
    public BaseDao(Class<T> po) {
        this.dbUtil = DBUtil.getInstance();
        connection = dbUtil.getConnection();
        this.po = po;
        this.sqlBuild = new Sql(po);
        allField = sqlBuild.getAllField();
        allFieldName = sqlBuild.getAllTableFieldName();
        primaryKey = sqlBuild.getTablePrimaryKey();
        primaryKeyName = sqlBuild.getTablePrimaryKeyName();
        try {
//            关闭自动提交
            connection.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    获取数据库的直接操作工具类
    public DBUtil getDbUtil() {
        return dbUtil;
    }

    //    获取sql语句构建器
    public Sql getSqlBuild() {
        return this.sqlBuild;
    }

    //    获取当前的数据库连接
    public Connection getConnection() {
        return connection;
    }

    //    新增数据
    @Override
    public boolean add(T t) {
        PreparedStatement statement = null;
        try {
//            构建插入语句
            String sql = sqlBuild.insert().build();
//            获取填充好值的 PreparedStatement
            statement = getStatement(connection.prepareStatement(sql), t);
            assert statement != null;
            final int i = statement.executeUpdate();
            if (i > 0) {
//                提交事务
                connection.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
//                错误就回滚事务
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            closeConnection(statement, null);
            closeConnection();
        }
        return false;
    }

    //    获取表中所有的数据
    @Override
    public List<T> list() {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
//            构建查询语句
            String sql = sqlBuild.select().build();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
//            返回解析好的类型对象
            return parseResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            关闭连接
            closeConnection(statement, resultSet);
            closeConnection();
        }
        throw new RuntimeException("没有查询到对应的信息");
    }

    //    分页查询
    @Override
    public List<T> pages(int current, int pageSize) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            //            构建分页查询语句
            String sql = sqlBuild.select().limit().and(pageSize).offset().and(pageSize * (current - 1)).build();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            return parseResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection(statement, resultSet);
            closeConnection();
        }
        throw new RuntimeException("没有找到当前分页的信息");
    }

    //    解析查询出来的结果(全部的字段)
    private List<T> parseResultSet(ResultSet resultSet) {
        final List<T> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
//                 实例化一个要填充内容的对象
                final T t = this.po.getConstructor().newInstance();
//                 拿到一行的数据
                final List<Object> data = allFieldName.stream().map(name -> {
                    Object o = null;
                    try {
                        o = resultSet.getObject(name);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return o;
                }).collect(Collectors.toList());
//                 填充属性
                for (int i = 0; i < allField.size(); i++) {
                    String name = allField.get(i).getName();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
//                     获取对应的set方法
                    final Method method = t.getClass().getMethod("set" + name, allField.get(i).getType());
//                     拿到对应的值,实体数据类型必须对应数据库中的类型
                    Object o = data.get(i);
//                    使用set方法注入值
                    method.invoke(t, o);
                }
                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //    解析查询出来的结果(指定的字段)
    private List<T> parseResultSet(ResultSet resultSet, Class<T> po, String[] data) {
        final List<T> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
//                通过无参构造器实例化
                final T t = po.getConstructor().newInstance();
                for (String s : data) {
//                    下划线转驼峰
                    final String name = StringUtil.convertToLineHump(s);
                    final Field field = po.getDeclaredField(name.substring(0, 1).toLowerCase() + name.substring(1));
                    final Method method = t.getClass().getDeclaredMethod("set" + name, field.getType());
                    method.invoke(t, resultSet.getObject(s));
                }
                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //    通过主键获取实体类
    @Override
    public <O> T getById(O id) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
//            拼接通过主键来获取信息的语句
            String sql = sqlBuild.select().where().eq(primaryKeyName, id).build();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            final List<T> list = parseResultSet(resultSet);
            if (list.size() > 1) throw new RuntimeException("当前主键有多个值");
            return list.get(0);
        } catch (Exception e) {
//        没查到(或者解析错误)就返回null
            return null;
        } finally {
//            关闭连接
            closeConnection(statement, resultSet);
            closeConnection();
        }
    }

    //    通过主键删除
    @Override
    public <O> boolean deleteById(O id) {
        PreparedStatement statement = null;
        try {
//            构建通过主键删除的语句
            String sql = sqlBuild.delete().where().eq(primaryKeyName, id).build();
            statement = connection.prepareStatement(sql);
            final int i = statement.executeUpdate();
            if (i > 0) {
                connection.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
//                回滚事务
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            closeConnection(statement, null);
            closeConnection();
        }
        return false;
    }

    //    通过实体类的主键更新
    @Override
    public boolean updateById(T t) {
        PreparedStatement statement = null;
        try {
            primaryKey.setAccessible(true);
//            构建通过主键更新的语句
            String sql = sqlBuild.update().where().eq(primaryKeyName, primaryKey.get(t)).build();
            primaryKey.setAccessible(false);
            statement = getStatement(connection.prepareStatement(sql), t);
            final int i = statement.executeUpdate();
            if (i > 0) {
                connection.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            closeConnection(statement, null);
            closeConnection();
        }
        return false;
    }

    //    通过自定义的sql语句查询
    @Override
    public List<T> query(String sql) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            if (sql.contains("select")) {
                sql = sql.replace("select", "SELECT");
            }
            if (sql.contains("from")) {
                sql = sql.replace("from", "FROM");
            }
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
//            获取参数
            final String params = sql.substring(sql.indexOf("SELECT") + 6, sql.indexOf("FROM")).trim();
//            *是查全部,所有直接解析就行
            if (params.equals("*")) {
                return parseResultSet(resultSet);
            } else {
//                根据传入的参数解析
                return parseResultSet(resultSet, po, params.split(","));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection(statement, resultSet);
            closeConnection();
        }
        throw new RuntimeException("执行sql语句出错");
    }

    //    执行增删改操作
    @Override
    public boolean exec(String sql) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            if (statement.executeUpdate() > 0) {
                connection.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            closeConnection(statement, null);
            closeConnection();
        }
        return false;
    }

    //    填充?的值
    private PreparedStatement getStatement(PreparedStatement statement, T t) {
//        遍历当前类的属性
        Utils.ListEach(allField, (field, index) -> {
            field.setAccessible(true);
            try {
//                从坐标为1的地方开始填充数据
                statement.setObject(index + 1, field.get(t));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(false);
            }
        });
        return statement;
    }

    //    关闭连接
    private void closeConnection(Statement statement, ResultSet resultSet) {
        try {
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    释放当前连接
    @Override
    public void closeConnection() {
        dbUtil.closeConnection(connection);
    }
}
