package com.shy.yourbatiscode.logging;
import com.shy.yourbatiscode.datasource.pooled.PooledDataSource;

/*
* 日志工厂类，这是单例的
* */
public class LogFactory {

    private LogFactory(){

    }

    public static Log getLog(Class<PooledDataSource> pooledDataSourceClass) {
        return new DefaultLog();
    }
}
