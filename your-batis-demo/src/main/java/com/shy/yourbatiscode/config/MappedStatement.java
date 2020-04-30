package com.shy.yourbatiscode.config;

/**
 * 保存sql语句信息的实体类
* */
public class MappedStatement {
    private String namespace;//命名空间
    private String sourceId;//id
    private String resultType;//返回值类型
    private String sql;//处理过的sql语句
    private String parameterType;//参数类型
    private  Object parameter;//可能带有的参数
    private String exeType;//执行的数据库操作类型

    public String getExeType() {
        return exeType;
    }

    public void setExeType(String exeType) {
        this.exeType = exeType;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public Object getParameter() {
        return parameter;
    }

    public void setParameter(Object parameter) {
        this.parameter = parameter;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
