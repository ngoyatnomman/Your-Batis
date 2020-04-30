package com.shy.yourbatiscode.session.sqlsession;

import com.shy.yourbatiscode.config.Configuration;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * mybatis 暴露给外部的接口，实现增删改查的能力
*@see DefaultSqlSession
* 1.对外提供数据访问的api
* 2.对内将请求转发给executor
* 3.？
* */
public interface SqlSession {

    /**根据传入的条件查询单一结果
    * @param statement 方法对应的sql语句，namespace+id
    * @param parameter 要传入sql语句中的查询参数
    * @return 返回指定的结果对象
    * */
    <T>T selectOne(String statement, Object parameter);

    <E>List<E> selectList(String statement, Object parameter);

    <T>T getMapper(Class<T> type);

    <T> T selectOne(String statement);
    
    <E> List<E> selectList(String statement);
    
    <K, V> Map<K, V> selectMap(String statement, String mapKey);

    <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey);
    
    int insert(String statement);

    int insert(String statement, Object parameter);

    int update(String statement);

    int update(String statement, Object parameter);

    int delete(String statement);

    int delete(String statement, Object parameter);

    void commit();

    void commit(boolean var);

    void rollback();

    void rollback(boolean var);
    
    void close();

    void clearCache();

    Configuration getConfiguration();
    
    Connection getConnection();
}
