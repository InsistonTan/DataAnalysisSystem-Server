package com.Tan.service;
import com.Tan.dao.UserDao;
import com.Tan.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @Author Tan
 * @Description
 * @Date 2020-05-01
 */
@Service
public class UserService {
    @Autowired
    UserDao userDao;
    //通过用户名获取id
    public String getIdByName(User user)
    {
        return userDao.getIdByName(user.getUsername());
    }
    //登陆
    public int loginCheck(User loginUser)
    {
        User temp=userDao.selectUser(loginUser.getUsername());
        if(temp==null)
            return -1;//用户不存在
        else if(temp.getPassword().equals(loginUser.getPassword()))
            return 1;//登录成功
        else return 0;//密码错误
    }
    //注册
    public int registerUser(User registerUser)
    {
        //
        int UID=0;
        //循环产生10001-20001的UID
        Random random=new Random();
        for(int i=0;i<10000;i++)
        {
            int randomID=random.nextInt(10000)+10001;//随机产生用户ID
            User u=userDao.selectUser(Integer.toString(randomID));
            if(u==null)
            {
                UID=randomID;
                registerUser.setId(Integer.toString(UID));
                break;
            }

        }
        //未能产生正确的UID
        if(UID==0)
            return 0;
        //注册时间
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String time=dateFormat.format(new Date());
        registerUser.setAddtime(time);
        //检查此用户名是否已存在
        User temp=userDao.selectUser(registerUser.getUsername());
        if(temp==null)
        {
            System.out.println("此用户名还未被注册");
            if(userDao.insertUser(registerUser))
            {
                return 1;//注册成功
            }
            else return 0;//注册失败

        }
        else
        {
            System.out.println("此用户名已被注册："+temp.getUsername());
            return -1;//用户已存在，注册失败
        }
    }

}
