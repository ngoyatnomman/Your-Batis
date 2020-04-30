package com.shy.yourbatiscode.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 反射工具类
* */
public class ReflectionUtil {

    /**@param bean 实体类
    * @param propName 属性名
    * @param value 属性值
    * 填充实体类某个属性值的方法
    * */
    public static void setPropToBean(Object bean,String propName,Object value){
        Field f;
        try {
            f = bean.getClass().getDeclaredField(propName);
            Method setMethod = getSetMethod(f);
            setMethod.invoke(bean,value);
        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param f 属性
     * @return Method
     * 根据属性获取对应的set方法*/
    public static Method getSetMethod(Field f) throws NoSuchMethodException {
        String fieldName = f.getName();
        StringBuffer sb = new StringBuffer("set");
        sb.append(fieldName.substring(0,1).toUpperCase());
        sb.append(fieldName.substring(1));
        return  f.getDeclaringClass().getMethod(sb.toString(),f.getType());
    }

    /**
     * @param f 属性
     * @return Method
     * 根据属性获取对应的get方法*/
    public static Method getGetMethod(Field f) throws NoSuchMethodException {
        String fieldName = f.getName();
        StringBuffer sb = new StringBuffer("get");
        sb.append(fieldName.substring(0,1).toUpperCase());
        sb.append(fieldName.substring(1));
        return f.getDeclaringClass().getMethod(sb.toString());
    }

    /**
     * @param entity 实体类
    * @param resultSet 结果集对象
    * 从resultSet中读取一行数据，并填充至指定的实体bean
    * */
    public static void setPropToBeanFromResultSet(Object entity, ResultSet resultSet) throws SQLException {
        Field[] declaredFields = entity.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            setPropToBean(entity,declaredField.getName(),resultSet.getObject(declaredField.getName()));
        }
    }

    /**
     * @param target 实体类对象
     * @param fieldName 属性名
     *根据属性名从对象中获取属性值
     * */
    public static Object getFieldValueByName(Object target,String fieldName) {
        Object result = null;
        Method getMethod = null;
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            getMethod = getGetMethod(field);
            result = getMethod.invoke(target);
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

}
