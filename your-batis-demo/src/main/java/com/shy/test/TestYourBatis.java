package com.shy.test;

import com.shy.mapper.UserMapper;
import com.shy.yourbatiscode.session.factory.SqlSessionFactoryBuilder;
import com.shy.pojo.User;
import com.shy.yourbatiscode.session.sqlsession.SqlSession;
import com.shy.yourbatiscode.session.factory.SqlSessionFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 *测试类
 * */
public class TestYourBatis {

    public static void main(String[] args) throws IOException{

        InputStream is = TestYourBatis.class.getClassLoader().getResourceAsStream("mineBatisConfig.xml");
        //1.实例化SqlSessionFactory，加载数据库配置文件以及mapper.xml文件到Configuration对象
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
        if(is != null){
            is.close();
        }
        //2.获取sqlSession对象
        SqlSession sqlSession = factory.openSession(false);
        //3.通过动态代理实现查询
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User userBean = new User();
        userBean.setUserId(3);
        userBean.setPassword("xiaoli");
        int i = userMapper.updateUser(7);
        System.out.println(i);

    }

}
