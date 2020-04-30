package com.shy.yourbatiscode.datasource.pooled;

import com.shy.yourbatiscode.config.Configuration;
import com.shy.yourbatiscode.datasource.unpooled.UnpooledDataSource;
import com.shy.yourbatiscode.logging.Log;
import com.shy.yourbatiscode.logging.LogFactory;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 PooledDataSource:一个简单的，同步的、线程安全的数据库连接池
 */
//使用连接池的数据源
public class PooledDataSource implements DataSource {

    private static final Log log = LogFactory.getLog(PooledDataSource.class);
    private final PoolState state = new PoolState(this);

    //真正用于创建连接的数据源
    private final UnpooledDataSource dataSource;

    // 可选配置字段
    //最大活跃连接数
    protected int poolMaximumActiveConnections = 10;
    //最大闲置连接数
    protected int poolMaximumIdleConnections = 5;
    //最大checkout时长（最长使用时间）
    protected int poolMaximumCheckoutTime = 20000;
    //无法取得连接时最大的等待时间
    protected int poolTimeToWait = 20000;
    //测试连接是否有效的sql语句，默认的语句是无效的，会报错
    protected String poolPingQuery = "NO PING QUERY SET";
    //是否允许测试连接
    protected boolean poolPingEnabled = false;
    //配置一段时间，当连接在这段时间内没有被使用，才允许测试连接是否有效
    protected int poolPingConnectionsNotUsedFor = 0;

    /*=====================================构造方法===================================*/
    public PooledDataSource(UnpooledDataSource dataSource){
        this.dataSource = dataSource;
    }

    public PooledDataSource(String driver, String url, String username, String password) {
        this.dataSource = new UnpooledDataSource(driver, url, username, password);
    }

    public PooledDataSource(Configuration conf) {
        this.dataSource = new UnpooledDataSource(conf);
    }



    /*=================================重写DataSource接口的方法=========================*/
    @Override
    public Connection getConnection() throws SQLException {
        return this.popConnection().getProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return this.popConnection().getProxyConnection();
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(this.getClass().getName() + " is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger("global");
    }

    /*=====================================提供给外界的方法===================================*/
    //回收连接资源
    protected void pushConnection(PooledConnection conn) throws SQLException {
        synchronized(this.state) {//回收连接必须是同步的
            this.state.activeConnections.remove(conn);//从活跃连接池中删除此连接
            if (conn.isValid()) {
                //判断闲置连接池资源是否已经达到上限
                if (this.state.idleConnections.size() < this.poolMaximumIdleConnections) {
                    //没有达到上限，进行回收
                    state.accumulatedCheckoutTime += conn.getCheckoutTime();
                    if (!conn.getRealConnection().getAutoCommit()) {
                        conn.getRealConnection().rollback();//如果还有事务没有提交，进行回滚操作
                    }
                    //基于该连接，创建一个新的连接资源，并刷新连接状态
                    PooledConnection newConn = new PooledConnection(conn.getRealConnection(), this);
                    state.idleConnections.add(newConn);
                    newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
                    newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
                    //老连接失效
                    conn.invalidate();
                    if (log.isDebugEnabled()) {
                        log.debug("Returned connection " + newConn.getRealHashCode() + " to pool.");
                    }
                    //唤醒其他被阻塞的线程
                    state.notifyAll();
                } else {
                    //如果闲置连接池已经达到上限了，将连接真实关闭
                    state.accumulatedCheckoutTime += conn.getCheckoutTime();
                    if (!conn.getRealConnection().getAutoCommit()) {
                        conn.getRealConnection().rollback();
                    }
                    //关闭真的数据库连接
                    conn.getRealConnection().close();
                    if (log.isDebugEnabled()) {
                        log.debug("Closed connection " + conn.getRealHashCode() + ".");
                    }
                    //将连接对象设置为无效
                    conn.invalidate();
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("A bad connection (" + conn.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
                }
                ++this.state.badConnectionCount;
            }
        }
    }

    //从连接池中获取资源
    private PooledConnection popConnection() throws SQLException {
        boolean countedWait = false;
        PooledConnection conn = null;
        long t = System.currentTimeMillis();//记录尝试获取连接的起始时间戳
        int localBadConnectionCount = 0;//初始化获取到无效连接的次数

        while(conn == null) {
            synchronized(state) {//获取连接必须是同步的
                if (state.idleConnections.size() > 0) {//检测是否有空闲连接
                    //有空闲连接直接使用
                    conn = state.idleConnections.remove(0);
                    if (log.isDebugEnabled()) {
                        log.debug("Checked out connection " + conn.getRealHashCode() + " from pool.");
                    }
                } else if (state.activeConnections.size() < this.poolMaximumActiveConnections) {//没有空闲连接，判断活跃连接池中的数量是否大于最大连接数
                    //没有则可创建新的连接
                    conn = new PooledConnection(this.dataSource.getConnection(), this);
                    if (log.isDebugEnabled()) {
                        log.debug("Created connection " + conn.getRealHashCode() + ".");
                    }
                } else {//如果已经等于最大连接数，则不能创建新连接
                    //获取最早创建的连接
                    PooledConnection oldestActiveConnection = state.activeConnections.get(0);
                    long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                    if (longestCheckoutTime > (long)this.poolMaximumCheckoutTime) {//检测是否已经超过最长使用时间
                        //如果超时，对超时连接的信息进行统计
                        ++state.claimedOverdueConnectionCount;//超时连接次数+1
                        state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;//累计超时时间增加
                        state.accumulatedCheckoutTime += longestCheckoutTime;//累计的使用连接的时间增加
                        state.activeConnections.remove(oldestActiveConnection);//从活跃队列中删除
                        if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {//如果超时连接未提交，则手动回滚
                            oldestActiveConnection.getRealConnection().rollback();
                        }
                        //在数据库中创建新的连接，注意对于数据库来说，并没有创建新连接；
                        conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
                        oldestActiveConnection.invalidate();//让老连接失效
                        if (log.isDebugEnabled()) {
                            log.debug("Claimed overdue connection " + conn.getRealHashCode() + ".");
                        }
                    } else {
                        //无空闲连接，最早创建的连接没有失效，无法创建新连接，只能阻塞
                        try {
                            if (!countedWait) {
                                ++state.hadToWaitCount;//连接池累计等待次数+1
                                countedWait = true;
                            }

                            if (log.isDebugEnabled()) {
                                log.debug("Waiting as long as " + this.poolTimeToWait + " milliseconds for connection.");
                            }

                            long wt = System.currentTimeMillis();
                            state.wait((long)this.poolTimeToWait);//阻塞等待指定时间
                            state.accumulatedWaitTime += System.currentTimeMillis() - wt;//累计等待时间增加
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }

                if (conn != null) {
                    //获取连接成功的，要测试连接是否有效，同时更新统计数据
                    if (conn.isValid()) {//检测连接是否有效
                        if (!conn.getRealConnection().getAutoCommit()) {
                            conn.getRealConnection().rollback();//如果遗留历史的事务，回滚
                        }
                        //连接池相关统计更新
                        conn.setCheckoutTimestamp(System.currentTimeMillis());
                        conn.setLastUsedTimestamp(System.currentTimeMillis());
                        this.state.activeConnections.add(conn);
                        ++this.state.requestCount;
                        this.state.accumulatedRequestTime += System.currentTimeMillis() - t;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
                        }

                        ++this.state.badConnectionCount;
                        ++localBadConnectionCount;
                        conn = null;
                        if (localBadConnectionCount > this.poolMaximumIdleConnections + 3) {
                            if (log.isDebugEnabled()) {
                                log.debug("PooledDataSource: Could not get a good connection to the database.");
                            }

                            throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                        }
                    }
                }
            }
        }

        if (conn == null) {
            if (log.isDebugEnabled()) {
                log.debug("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
            }

            throw new SQLException("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
        } else {
            return conn;
        }
    }

    /**
     * 测试连接用
    * */
    protected boolean pingConnection(PooledConnection conn) {
        boolean result = true;
//
//        try {
//            result = !conn.getRealConnection().isClosed();
//        } catch (SQLException var8) {
//            if (log.isDebugEnabled()) {
//                log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + var8.getMessage());
//            }
//
//            result = false;
//        }
//
//        if (result && this.poolPingEnabled && this.poolPingConnectionsNotUsedFor >= 0 && conn.getTimeElapsedSinceLastUse() > (long)this.poolPingConnectionsNotUsedFor) {
//            try {
//                if (log.isDebugEnabled()) {
//                    log.debug("Testing connection " + conn.getRealHashCode() + " ...");
//                }
//                Connection realConn = conn.getRealConnection();
//                Statement statement = realConn.createStatement();
//                ResultSet rs = statement.executeQuery(this.poolPingQuery);
//                rs.close();
//                statement.close();
//                if (!realConn.getAutoCommit()) {
//                    realConn.rollback();
//                }
//
//                result = true;
//                if (log.isDebugEnabled()) {
//                    log.debug("Connection " + conn.getRealHashCode() + " is GOOD!");
//                }
//            } catch (Exception e) {
//                log.warn("Execution of ping query '" + this.poolPingQuery + "' failed: " + e.getMessage());
//                try {
//                    conn.getRealConnection().close();
//                } catch (Exception e1) {
//                    log.warn("Error close connection.");
//                }
//                result = false;
//                if (log.isDebugEnabled()) {
//                    log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
//                }
//            }
//        }

        return result;
    }

    public String getDriver() {
        return this.dataSource.getDriver();
    }

    public String getUrl() {
        return this.dataSource.getUrl();
    }

    public String getUsername() {
        return this.dataSource.getUsername();
    }

    public String getPassword() {
        return this.dataSource.getPassword();
    }

}

