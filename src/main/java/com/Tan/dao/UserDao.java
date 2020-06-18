package com.Tan.dao;
import com.Tan.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
/*
 * @Description:用户的持久层接口
 * @author:TanJifeng
 * @date:null
 * */
@Repository
public interface UserDao {
    //通过用户名获取uid
    @Select("select uid from users where username=#{name}")
    String getIdByName(@Param("name") String name);

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
    @Insert("insert into users(uid,username,password,addtime) values (#{uid},#{username},#{password},#{addtime})")
    boolean insertUser(User users);

    @Select("select * from users")
    List<User> selectAll();
}