package com.shy.yourbatiscode.binding;

import com.shy.yourbatiscode.annotation.Param;
import com.shy.yourbatiscode.config.Configuration;
import com.shy.yourbatiscode.config.MappedStatement;
import com.shy.yourbatiscode.session.sqlsession.SqlSession;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 代理类业务处理
*/
public class MapperProxy implements InvocationHandler {

    private SqlSession session;
    private Configuration configuration;

    public MapperProxy(SqlSession session, Configuration configuration) {
        this.session = session;
        this.configuration = configuration;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass() != Object.class) {
            String sourceId = method.getDeclaringClass().getName()+ "." + method.getName();
            MappedStatement ms = configuration.getMappedStatements().get(sourceId);
            Map<String, Object> map = new HashMap<>();//把入参封装成map
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {//扫描所有参数中带注解的参数
                map.put(String.valueOf(i),args[i]);//0，1类型
                map.put("param"+i,args[i]);//param0，param1类型
                if (parameters[i].isAnnotationPresent(Param.class)) {
                    Param annotation = parameters[i].getAnnotation(Param.class);
                    map.put(annotation.value(), args[i]);
                }
            }
            switch (ms.getExeType()){//判断执行的操作类型，转发到相应的方法
                case "select":
                    if (Collection.class.isAssignableFrom(method.getReturnType())) {//判断返回参数是不是collection的子类
                        return session.selectList(sourceId, map);
                    } else {//否则执行查一条
                        return session.selectOne(sourceId, map);
                    }
                case "insert":
                    return session.insert(sourceId,map);
                case "delete":
                    return session.delete(sourceId,map);
                case "update":
                    return session.update(sourceId,map);
            }
            return null;
        }else{
            return null;
        }
    }
}
