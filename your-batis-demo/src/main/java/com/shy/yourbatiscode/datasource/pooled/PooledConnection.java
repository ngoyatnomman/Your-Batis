package com.shy.yourbatiscode.datasource.pooled;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 通过动态代理封装了真正的数据库连接对象
 * */
public class PooledConnection implements InvocationHandler {
    //一些常量可以提高性能
    private static final String CLOSE = "close";//关闭的方法名
    private static final Class<?>[] IFACES = new Class[]{Connection.class};//获取动态代理的第二个参数

    private PooledDataSource dataSource;//池化数据源
    private Connection realConnection;//真实的Connection类型的连接
    private Connection proxyConnection;//代理连接
    private long checkoutTimestamp;//使用连接的时间
    private int hashCode = 0;//未知
    private long createdTimestamp;//创建连接的时间
    private long lastUsedTimestamp;//最后使用连接时间
    private boolean valid;//判断连接是否有效的标志位

    /*=====================================构造方法===================================*/
    public PooledConnection(Connection connection, PooledDataSource dataSource) {
        this.hashCode = connection.hashCode();
        this.realConnection  = connection;
        this.dataSource = dataSource;
        this.createdTimestamp = System.currentTimeMillis();//初始创建时间为系统当前时间
        this.lastUsedTimestamp = System.currentTimeMillis();//初始最后使用时间为系统当前时间
        this.valid = true;//初始为连接有效
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),IFACES,this);
    }

    /*=====================================普通getter setter方法===================================*/
    public Connection getRealConnection() {
        return realConnection;
    }

    public Connection getProxyConnection(){
        return this.proxyConnection;
    }

    public long getCreatedTimestamp() {
        return this.createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getLastUsedTimestamp() {
        return this.lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    public long getCheckoutTimestamp() {
        return checkoutTimestamp;
    }

    public void setCheckoutTimestamp(long timestamp) {
        this.checkoutTimestamp = timestamp;
    }

    /*=====================================提供给外界的方法===================================*/
    public int getRealHashCode() {
        return this.realConnection == null ? 0:this.realConnection.hashCode();
    }

    public long getTimeElapsedSinceLastUse() {//获取从最后使用到现在经历的时间
        return System.currentTimeMillis() - this.lastUsedTimestamp;
    }

    public long getCheckoutTime() {
        return System.currentTimeMillis() - this.checkoutTimestamp;
    }

    public void invalidate() {
        this.valid = false;
    }

    public boolean isValid() {
        return this.valid && this.realConnection != null && this.dataSource.pingConnection(this);
    }

    /*=====================================重写Object的方法===================================*/
    @Override
    public int hashCode(){
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PooledConnection) {
            return this.realConnection.hashCode() == ((PooledConnection)obj).realConnection.hashCode();
        } else if (obj instanceof Connection) {
            return this.hashCode == obj.hashCode();
        } else {
            return false;
        }
    }

    /*=================================重写InvocationHandler接口的方法=========================*/
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(CLOSE.equals(method.getName())){
            this.dataSource.pushConnection(this);
            return null;
        }
        if(method.getDeclaringClass() != Object.class){//排除从父类Objcet中继承的方法
            checkConnection();
        }
        return method.invoke(this.realConnection,args);
    }

    /*=====================================私有的方法===================================*/
    private void checkConnection() throws SQLException {//如果连接无效就抛异常
        if(!this.valid){
            throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
        }
    }
}
