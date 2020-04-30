package com.shy.yourbatiscode.logging;

/**
 * 日志接口
 * */
public interface Log {

    boolean isDebugEnabled();

    void debug(String s);

    void warn(String s);
}
