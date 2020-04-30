package com.shy.yourbatiscode.executor;

import com.shy.yourbatiscode.config.MappedStatement;

import java.util.List;

/**
 * 与数据库进行直接交互
 * @see DefaultExecutor
 * */
public interface Executor {

    /**
     * 最终查询方法
     * */
    <E>List<E> query(MappedStatement ms, Object parameter);

    int update(MappedStatement ms, Object parameter);
}
