package com.shy.yourbatiscode.xmlbuilder;

import com.shy.yourbatiscode.config.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.util.List;

/**
 * 解析全局配置XML
 * */
public class XMLConfigBuilder {

    private InputStream inputStream;
    //加final内存地址不可更改,全局唯一
    private final Configuration configuration = new Configuration();

    public XMLConfigBuilder(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Configuration parse(){
        parseConfigXml();
        return configuration;
    }

    //解析全局配置文件配置信息
    private void parseConfigXml(){
        //加载数据库信息配置文件
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);//这一步是跳过dtd验证带来的报错
            document = reader.read(inputStream);
        } catch (DocumentException | SAXException e) {
            e.printStackTrace();
        }
        Element root = document.getRootElement();
        Element dataSource = root.element("dataSource");
        Element mappers = root.element("mappers");
        //将数据库配置信息写入configuration对象
        configuration.setDataSourceType(dataSource.attribute("type").getData().toString());
        String driver = dataSource.element("driver").attribute("value").getData().toString();
        String url = dataSource.element("url").attribute("value").getData().toString();
        String username = dataSource.element("username").attribute("value").getData().toString();
        String password = dataSource.element("password").attribute("value").getData().toString();
        configuration.setJdbcDriver(driver);
        configuration.setJdbcPassword(password);
        configuration.setJdbcUrl(url);
        configuration.setJdbcUsername(username);
        List<Element> elements = mappers.elements("mapper");
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(configuration);
        for (Element element : elements) {
            String resource = element.attribute("resource").getData().toString();
            xmlMapperBuilder.parse(resource);
        }
    }
}
