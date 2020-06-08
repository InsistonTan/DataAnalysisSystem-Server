package com.Tan.dao;

import com.Tan.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao {
    //通过用户名获取id
    @Select("select id from users where username=#{name}")
    String getIdByName(@Param("name") String name);

    /**
     * 查询用户，用于登录
     * @return
     */
    @Select("select * from users where username=#{username} and password=#{password}")
    User findUserByUsernameAndPassword(User user);
    /**
     * 根据用户名查询用户，判断用户是否存在
     * @param username
     * @return
     */
    @Select("select * from users where username=#{username}")
    User selectUser(@Param("username") String username);

    /**
     * 注册一个用户
     * @param users
     * @return
     */
    @Insert("insert into users(id,username,password,addtime) values (#{user.id},#{user.username},#{user.password},#{user.addtime})")
    boolean insertUser(@Param("user") User users);

    @Select("select * from users")
    List<User> selectAll();
}