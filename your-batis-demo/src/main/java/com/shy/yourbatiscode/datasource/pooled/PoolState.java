package com.shy.yourbatiscode.datasource.pooled;

/**
 PoolState:用于管理PooledConnection对象状态的组件，通过两个list分别
 管理空闲状态的连接资源和活跃状态的连接资源
 */
import java.util.ArrayList;
import java.util.List;

public class PoolState {
    protected PooledDataSource dataSource;
    //空闲的连接池资源集合
    protected final List<PooledConnection> idleConnections = new ArrayList();
    //活跃的连接池资源集合
    protected final List<PooledConnection> activeConnections = new ArrayList();
    //请求的次数
    protected long requestCount = 0L;
    //累计的获得连接的时间
    protected long accumulatedRequestTime = 0L;
    //累计的使用连接的时间
    protected long accumulatedCheckoutTime = 0L;
    //使用连接超时的次数
    protected long claimedOverdueConnectionCount = 0L;
    //累计超时时间
    protected long accumulatedCheckoutTimeOfOverdueConnections = 0L;
    //累计等待时间
    protected long accumulatedWaitTime = 0L;
    //等待次数
    protected long hadToWaitCount = 0L;
    //无效的连接次数
    protected long badConnectionCount = 0L;

    /*=====================================构造方法===================================*/
    public PoolState(PooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*=====================================普通getter setter方法===================================*/
    public synchronized long getRequestCount() {
        return this.requestCount;
    }

    public synchronized long getHadToWaitCount() {
        return this.hadToWaitCount;
    }

    public synchronized long getBadConnectionCount() {
        return this.badConnectionCount;
    }

    public synchronized long getClaimedOverdueConnectionCount() {
        return this.claimedOverdueConnectionCount;
    }

    /*=====================================提供给外界的方法===================================*/
    public synchronized long getAverageRequestTime() {//平均请求时间
        return this.requestCount == 0L ? 0L : this.accumulatedRequestTime / this.requestCount;
    }

    public synchronized long getAverageWaitTime() {//平均等待时间
        return this.hadToWaitCount == 0L ? 0L : this.accumulatedWaitTime / this.hadToWaitCount;
    }

    public synchronized long getAverageOverdueCheckoutTime() {//平均超时时间
        return this.claimedOverdueConnectionCount == 0L ? 0L : this.accumulatedCheckoutTimeOfOverdueConnections / this.claimedOverdueConnectionCount;
    }

    public synchronized long getAverageCheckoutTime() {//平均使用连接的时间
        return this.requestCount == 0L ? 0L : this.accumulatedCheckoutTime / this.requestCount;
    }

    public synchronized int getIdleConnectionCount() {//空闲连接池大小
        return this.idleConnections.size();
    }

    public synchronized int getActiveConnectionCount() {//活跃连接池大小
        return this.activeConnections.size();
    }

    /*=====================================重写Object的方法===================================*/
    public synchronized String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n===CONFINGURATION==============================================");
        buffer.append("\n jdbcDriver                     ").append(this.dataSource.getDriver());
        buffer.append("\n jdbcUrl                        ").append(this.dataSource.getUrl());
        buffer.append("\n jdbcUsername                   ").append(this.dataSource.getUsername());
        buffer.append("\n jdbcPassword                   ").append(this.dataSource.getPassword() == null ? "NULL" : "************");
        buffer.append("\n poolMaxActiveConnections       ").append(this.dataSource.poolMaximumActiveConnections);
        buffer.append("\n poolMaxIdleConnections         ").append(this.dataSource.poolMaximumIdleConnections);
        buffer.append("\n poolMaxCheckoutTime            ").append(this.dataSource.poolMaximumCheckoutTime);
        buffer.append("\n poolTimeToWait                 ").append(this.dataSource.poolTimeToWait);
        buffer.append("\n poolPingEnabled                ").append(this.dataSource.poolPingEnabled);
        buffer.append("\n poolPingQuery                  ").append(this.dataSource.poolPingQuery);
        buffer.append("\n poolPingConnectionsNotUsedFor  ").append(this.dataSource.poolPingConnectionsNotUsedFor);
        buffer.append("\n ---STATUS-----------------------------------------------------");
        buffer.append("\n activeConnections              ").append(this.getActiveConnectionCount());
        buffer.append("\n idleConnections                ").append(this.getIdleConnectionCount());
        buffer.append("\n requestCount                   ").append(this.getRequestCount());
        buffer.append("\n averageRequestTime             ").append(this.getAverageRequestTime());
        buffer.append("\n averageCheckoutTime            ").append(this.getAverageCheckoutTime());
        buffer.append("\n claimedOverdue                 ").append(this.getClaimedOverdueConnectionCount());
        buffer.append("\n averageOverdueCheckoutTime     ").append(this.getAverageOverdueCheckoutTime());
        buffer.append("\n hadToWait                      ").append(this.getHadToWaitCount());
        buffer.append("\n averageWaitTime                ").append(this.getAverageWaitTime());
        buffer.append("\n badConnectionCount             ").append(this.getBadConnectionCount());
        buffer.append("\n===============================================================");
        return buffer.toString();
    }
}

