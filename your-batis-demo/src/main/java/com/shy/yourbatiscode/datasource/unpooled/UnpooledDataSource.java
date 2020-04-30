package com.shy.yourbatiscode.datasource.unpooled;

import com.shy.yourbatiscode.config.Configuration;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 不使用池化技术，使用传统jdbc的数据源
 * */
public class UnpooledDataSource implements DataSource {
    private Properties driverProperties;
    private String driver;
    private String url;
    private String username;
    private String password;
    private Boolean autoCommit;
    private Integer defaultTransactionIsolationLevel;


    public UnpooledDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(Configuration conf) {
        this.driver = conf.getJdbcDriver();
        this.url = conf.getJdbcUrl();
        this.username = conf.getJdbcUsername();
        this.password = conf.getJdbcPassword();
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username,password);
    }

    private Connection doGetConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user",username);
        props.setProperty("password",password);
        return doGetConnection(props);
    }

    private Connection doGetConnection(Properties props) throws SQLException {
        initializeDriver();
        return DriverManager.getConnection(url, props);
    }

    private void initializeDriver() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username,password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
    }

    public void setDriverProperties(Properties driverProps) {
        this.driverProperties = driverProps;
    }

    public void setAutoCommit(boolean defaultAutoCommit) {
        this.autoCommit = defaultAutoCommit;
    }

    public boolean isAutoCommit() {
        return this.autoCommit;
    }

    public Integer getDefaultTransactionIsolationLevel() {
        return this.defaultTransactionIsolationLevel;
    }

    public Properties getDriverProperties() {
        return this.driverProperties;
    }
}
