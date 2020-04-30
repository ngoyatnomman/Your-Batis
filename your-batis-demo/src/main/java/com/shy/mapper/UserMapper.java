package com.shy.mapper;

import com.shy.yourbatiscode.annotation.Param;
import com.shy.pojo.User;
import java.util.List;

public interface UserMapper {

    List<Integer> selectAll();

    User selectById(Integer id);

    User selectByIdPwd(@Param("userId") Integer id, @Param("password") String password);

    int addUser(User user);

    int updateUser(Integer userId);

    int deleteUser(Integer userId);
}
