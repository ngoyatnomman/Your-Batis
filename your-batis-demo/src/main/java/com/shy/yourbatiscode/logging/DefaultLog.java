package com.shy.yourbatiscode.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 默认的日志实现类
 * */
public class DefaultLog  implements Log{
    private boolean debugEnabled = true;//为true则开启日志

    @Override
    public boolean isDebugEnabled() {
        return this.debugEnabled;
    }

    @Override
    public void debug(String s) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:dd");
        String dateStr = dateFormat.format(date);
        System.out.println(dateStr+"--[debug]:::::"+s);
    }

    @Override
    public void warn(String s) {
        System.out.println("[warn]:"+s);
    }
}
