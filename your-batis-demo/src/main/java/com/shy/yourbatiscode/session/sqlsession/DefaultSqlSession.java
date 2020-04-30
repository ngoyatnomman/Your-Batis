package com.shy.yourbatiscode.session.sqlsession;

import com.shy.yourbatiscode.binding.MapperProxy;
import com.shy.yourbatiscode.config.Configuration;
import com.shy.yourbatiscode.config.MappedStatement;
import com.shy.yourbatiscode.executor.DefaultExecutor;
import com.shy.yourbatiscode.executor.Executor;
import com.shy.yourbatiscode.reflection.ReflectionUtil;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sqlsession接口的默认实现
 * 1.对外提供数据访问的api
 * 2.对内将请求转发给executor
 * 3.？
* */
public class DefaultSqlSession implements SqlSession {
    //所有SqlSession都带有一个唯一的Config对象
    private final Configuration conf;
    //将请求转发到executor
    private Executor executor;
    //数据源对象
    private DataSource dataSource;

    public DefaultSqlSession(Configuration conf, DataSource dataSource) {
        this.conf = conf;
        this.dataSource = dataSource;
        this.executor = new DefaultExecutor(conf,dataSource);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        //执行sql语句获取查询结果
        List<Object> selectList = this.selectList(statement, parameter);
        //查询结果为空返回null
        if(selectList == null || selectList.size() == 0){
            return null;
        }
        if(selectList.size() == 1){//查询结果只有一个元素，返回该元素
            return (T)selectList.get(0);
        }else{//查询结果有多个元素，抛异常
            throw new RuntimeException("Too Many Results!");
        }
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        MappedStatement ms = conf.getMappedStatements().get(statement);
        if(parameter instanceof Map){//如果参数类型是Map，强制转为map
            ms.setParameterType("map");
        }
        return executor.query(ms,parameter);
    }

    /**
     * @param type 需要被代理的接口class
     * 获取mapper接口代理类
     * */
    @Override
    public <T> T getMapper(Class<T> type) {
        MapperProxy mp = new MapperProxy(this,conf);
        return (T) Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},mp);
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement,null);
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return this.selectList(statement,null);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.selectMap(statement,null,mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        List<Object> rsList = this.selectList(statement, parameter);
        Map<Object, Object> map = new HashMap<>();
        for (Object o : rsList) {
            if(o instanceof Map){//map获取mapKey对应的值作为键名
                Map temp = (Map)o;
                map.put(temp.get(mapKey),o);
            }else if(o instanceof Number){//如果是包装类
                throw new RuntimeException("无法产生map");
            }else{//实体类获取mapKey对应的属性作为键名(如果是Integer?)
                Object key = ReflectionUtil.getFieldValueByName(o,mapKey);
                map.put(key,o);
            }
        }
        return (Map<K, V>) map;
    }

    @Override
    public int insert(String statement) {
        return this.insert(statement,null);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return this.update(statement,parameter);
    }

    @Override
    public int update(String statement) {
        return this.update(statement,null);
    }

    @Override
    public int update(String statement, Object parameter) {
        MappedStatement ms = this.conf.getMappedStatements().get(statement);
        return executor.update(ms,parameter);
    }

    @Override
    public int delete(String statement) {
        return this.delete(statement,null);
    }

    @Override
    public int delete(String statement, Object parameter) {
        return this.update(statement,parameter);
    }

    @Override
    public void commit() {

    }

    @Override
    public void commit(boolean var) {

    }

    @Override
    public void rollback() {

    }

    @Override
    public void rollback(boolean var) {

    }

    /**
     * 关闭方法，需要进一步销毁连接池
     * */
    @Override
    public void close() {

    }

    @Override
    public void clearCache() {

    }

    @Override
    public Configuration getConfiguration() {
        return this.conf;
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
