package com.shy.yourbatiscode.executor;

import com.shy.yourbatiscode.config.Configuration;
import com.shy.yourbatiscode.config.MappedStatement;
import com.shy.yourbatiscode.reflection.ReflectionUtil;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认Executor实现类
 * */
public class DefaultExecutor implements Executor {

    private final Configuration conf;
    private DataSource dataSource;

    public DefaultExecutor(Configuration conf, DataSource dataSource) {
        this.conf = conf;
        this.dataSource = dataSource;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter) {
        List<E> ret = new ArrayList<>();//定义返回结果集
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(ms.getSql());
            //处理sql语句中占位符
            parameterize(ms,preparedStatement,parameter);
            resultSet = preparedStatement.executeQuery();
            //将结果集通过反射技术，填充到list中
            handlerResultSet(resultSet,ret,ms.getResultType());
        } catch (SQLException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }finally {
            try {
                if(resultSet != null){
                    resultSet.close();
                }
                if(preparedStatement != null){
                    preparedStatement.close();
                }
                if(connection != null){
                    connection.close();//如果使用了连接池，其实并不是直接关闭
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) {
        int result = 0;
        Connection connection = null;
        PreparedStatement pstm = null;
        try{
            connection = dataSource.getConnection();
            pstm = connection.prepareStatement(ms.getSql());
            connection.setAutoCommit(conf.isAutoCommit());//重新设置连接的自动提交属性
            parameterize(ms,pstm,parameter);//处理参数
            result = pstm.executeUpdate();
        } catch (IllegalAccessException | SQLException | NoSuchFieldException e) {
            e.printStackTrace();
        }finally {
            try {
                if(connection != null){
                    connection.close();//如果使用了连接池，其实并不是直接关闭
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
    * 结果集映射处理器
    * */
    private <E> void handlerResultSet(ResultSet resultSet, List<E> ret, String className) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        switch (className){
            case "Integer":
            case "int":
            case "String":
            case "Double":
            case "double":
            case "Float":
            case "float":
            case "Byte":
            case "byte"://基本数据类型直接加入集合
                while(resultSet.next()){
                    ret.add((E) resultSet.getObject(1));
                }
                break;
            case "map"://map类型注入
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while(resultSet.next()){
                    Map<String,Object> map = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        map.put(metaData.getColumnName(i),resultSet.getObject(i));
                    }
                    ret.add((E) map);
                }
                break;
            default://实体类注入
                Class<E> clazz = null;
                try {
                    clazz = (Class<E>) Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                while(resultSet.next()){
                    //通过反射实例化对象
                    Object entity = clazz.getDeclaredConstructor().newInstance();
                    ReflectionUtil.setPropToBeanFromResultSet(entity,resultSet);
                    ret.add((E) entity);
                }
                break;
        }
    }

    /**
     * 参数注入处理器
     * */
    private void parameterize(MappedStatement ms, PreparedStatement preparedStatement,Object parameter) throws SQLException, NoSuchFieldException, IllegalAccessException {
        Object paramObj = ms.getParameter();//sql的参数
        String parameterType = ms.getParameterType();//参数类型
        if(paramObj instanceof ArrayList){//sql参数不止一个
            List<String> paramNames = (ArrayList<String>)paramObj;
            if("map".equals(parameterType)){//参数类型是map
                Map paramType = (Map)parameter;
                for (int i = 0; i < paramNames.size(); i++) {
                    Object value = paramType.get(paramNames.get(i));
                    preparedStatement.setObject(i+1,value);
                }
            }else{//参数类型是bean
                Class paramClass = parameter.getClass();
                for (int i = 0; i < paramNames.size(); i++) {
                    Object value = ReflectionUtil.getFieldValueByName(parameter,paramNames.get(i));
                    preparedStatement.setObject(i+1,value);
                }
            }
        }else if(paramObj != null){//sql有一个参数
            String paramName = (String) paramObj;//object类型必须强转为String类型
            Object value = null;
            switch (parameterType){
                case "Integer":
                case "int":
                case "String":
                case "Double":
                case "double":
                case "Float":
                case "float":
                case "Byte":
                case "byte":
                    preparedStatement.setObject(1,parameter);
                    break;
                case "map":
                    Map paramType = (Map)parameter;
                    value = paramType.get(paramName);
                    preparedStatement.setObject(1,value);
                    break;
                default:
                    value = ReflectionUtil.getFieldValueByName(parameter, paramName);
                    preparedStatement.setObject(1,value);
                    break;
            }
        }//sql没有参数不做处理
    }
}
