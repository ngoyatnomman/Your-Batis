package com.shy.yourbatiscode.xmlbuilder;

import com.shy.yourbatiscode.config.Configuration;
import com.shy.yourbatiscode.config.MappedStatement;
import org.dom4j.Attribute;
import org.dom4j.Element;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析增删改查节点
 * */
public class XMLStatementBuilder {

    private Configuration conf;
    private String namespace;

    public XMLStatementBuilder(Configuration conf, String namespace) {
        this.conf = conf;
        this.namespace = namespace;
    }

    public void parse(Element element){
        MappedStatement mappedStatement = new MappedStatement();//实例化mappedStatement
        String exeType = element.getName();//标签名
        mappedStatement.setExeType(exeType);
        String id = element.attribute("id").getData().toString();
        Attribute paramTypeAttr = element.attribute("parameterType");
        if(paramTypeAttr != null){
            String parameterType = paramTypeAttr.getData().toString();
            mappedStatement.setParameterType(parameterType);
        }
        Attribute resultTypeAttr = element.attribute("resultType");
        if(resultTypeAttr != null){
            String resultType = resultTypeAttr.getData().toString();
            mappedStatement.setResultType(resultType);
        }
        String sql = element.getData().toString();//读取SQL语句信息
        String sourceId = namespace + "." + id;
        List<String> list = new ArrayList<>();
        sql = replacePlaceHolder(sql,list,"#{","}");
        if(list.size() == 0){
            mappedStatement.setParameter(null);
        }else if(list.size() == 1){
            mappedStatement.setParameter(list.get(0));
        }else{
            mappedStatement.setParameter(list);
        }
        //给mappedStatement属性复制
        mappedStatement.setSourceId(sourceId);
        mappedStatement.setNamespace(namespace);
        mappedStatement.setSql(sql);
        conf.getMappedStatements().put(sourceId,mappedStatement);//注册到configuration对象中
    }

    /**
     * 用于替换sql语句为占位符，并返回
     */
    private String replacePlaceHolder(String sql, List<String> props , String prefix, String suffix){
        int firstIndex = -1;
        int lastIndex = -1;
        String prop = null;
        StringBuffer sb  = new StringBuffer();
        while(true){
            sql = sql.substring(lastIndex+1);
            firstIndex = sql.indexOf(prefix);
            if(firstIndex == -1){
                break;
            }
            lastIndex = sql.indexOf(suffix,firstIndex+1);
            prop = sql.substring(firstIndex+2,lastIndex);
            props.add(prop);
            sb.append(sql.substring(0,firstIndex));
            sb.append("?");
        }
        sb.append(sql);
        return sb.toString();
    }

}
