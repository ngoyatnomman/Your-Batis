package com.shy.yourbatiscode.session.factory;

import com.shy.yourbatiscode.xmlbuilder.XMLConfigBuilder;
import java.io.InputStream;

public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(InputStream in){
        XMLConfigBuilder parser = new XMLConfigBuilder(in);
        return new SqlSessionFactory(parser.parse());
    }
}
