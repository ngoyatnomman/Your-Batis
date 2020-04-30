package com.shy.yourbatiscode.xmlbuilder;

import com.shy.yourbatiscode.config.Configuration;
import com.shy.yourbatiscode.session.factory.SqlSessionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 解析MapperXML
 * */
public class XMLMapperBuilder {

    private Configuration conf;

    public XMLMapperBuilder(Configuration conf) {
        this.conf = conf;
    }

    //加载指定文件夹下的所有mapper.xml
    public void getAllFiles(String mapperConfigLocation){
        URL resurces = SqlSessionFactory.class.getClassLoader().getResource(mapperConfigLocation);
        File mappers = new File(resurces.getFile());
        if(mappers.isDirectory()){
            File[] listFiles = mappers.listFiles();
            //遍历文件夹下所有的mapper.xml，解析信息后，注册到conf对象中
            for (File file : listFiles) {
                loadMapperXML(file);
            }
        }
    }

    //加载指定的mapper.xml文件
    public void loadMapperXML(File file){
        SAXReader reader = new SAXReader();
        Document document = null;
        try{
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);//这一步是跳过dtd验证带来的报错
            document = reader.read(file);
        } catch (DocumentException | SAXException | MalformedURLException e) {
            e.printStackTrace();
        }
        Element root = document.getRootElement();//获取根节点元素对象
        String namespace = root.attribute("namespace").getData().toString();//获取命名空间
        List<Element> elements = root.elements();//获取子节点列表
        XMLStatementBuilder xmlStatementBuilder = new XMLStatementBuilder(conf,namespace);
        for (Element element : elements) {//遍历select节点，将信息记录到MappedStatement对象，并登记到configuration对象中
            xmlStatementBuilder.parse(element);
        }
    }

    public void parse(String resource) {
        if(resource.endsWith("*.xml")){
            String mapperLocation = resource.substring(0,resource.indexOf("*.xml"));
            this.getAllFiles(mapperLocation);
        }else{
            File file = new File(this.getClass().getClassLoader().getResource(resource).getFile());
            this.loadMapperXML(file);
        }
    }
}
