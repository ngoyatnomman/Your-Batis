package com.shy.yourbatiscode.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 储存全局的配置信息的实体类
* */
public class Configuration {
    private String jdbcDriver;//数据库驱动
    private String jdbcUrl;//数据库url
    private String jdbcUsername;//数据库用户名
    private String jdbcPassword;//数据库密码
    private Map<String, MappedStatement> mappedStatements = new HashMap<>();//MappedStatement集合
    private String dataSourceType;//数据源类型，值为POOLED或UNPOOLED
    private boolean autoCommit;//自动提交

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public void setJdbcUsername(String jdbcUsername) {
        this.jdbcUsername = jdbcUsername;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }

    public Map<String, MappedStatement> getMappedStatements() {
        return mappedStatements;
    }

    public void setMappedStatements(Map<String, MappedStatement> mappedStatements) {
        this.mappedStatements = mappedStatements;
    }
}
