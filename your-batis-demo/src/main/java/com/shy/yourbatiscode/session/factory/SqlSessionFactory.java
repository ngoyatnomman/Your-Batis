package com.shy.yourbatiscode.session.factory;

import com.shy.yourbatiscode.config.Configuration;
import com.shy.yourbatiscode.datasource.pooled.PooledDataSource;
import com.shy.yourbatiscode.datasource.unpooled.UnpooledDataSource;
import com.shy.yourbatiscode.session.sqlsession.DefaultSqlSession;
import com.shy.yourbatiscode.session.sqlsession.SqlSession;

import javax.sql.DataSource;

/**
 * 1.实例化的过程中加载配置文件到configuration
 * 2.生产sqlSession
 * */
public class SqlSessionFactory {

    private Configuration configuration;

    public SqlSessionFactory(Configuration conf) {
        this.configuration = conf;
    }

    /**
     * 根据不同的配置创建不同类型的数据源对象
     * */
    public SqlSession openSession(){
        return this.openSession(false);//默认自动提交是false
    }

    public SqlSession openSession(boolean autoCommit){
        configuration.setAutoCommit(autoCommit);
        DataSource dataSource = null;
        if("UNPOOLED".equals(configuration.getDataSourceType())){
            dataSource = new UnpooledDataSource(configuration);
        }else if("POOLED".equals(configuration.getDataSourceType())){
            dataSource = new PooledDataSource(configuration);
        }
        return new DefaultSqlSession(configuration,dataSource);
    }
}
