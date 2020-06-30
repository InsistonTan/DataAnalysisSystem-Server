package com.Tan.service;
import com.Tan.dao.UserDao;
import com.Tan.domain.User;
import com.Tan.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/*
 * class:UserService
 * author:TanJifeng
 * last-update:2020-05-01
 * */
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
        User user=userDao.selectUser(loginUser.getUsername());
        if(user==null)
            return -1;//用户不存在
        //验证密码是否正确
        else if((!loginUser.getUsername().equals("admin"))&&MD5Utils.verify(user.getPassword(),loginUser.getPassword()))
            return 1;//登录成功
        else if(loginUser.getUsername().equals("admin")&&loginUser.getPassword().equals(user.getPassword()))
            return 1;//管理员登陆成功
        else return 0;//密码错误
    }

    //注册
    public int registerUser(User registerUser)
    {
        //将密码加密
        String pass=registerUser.getPassword();
        registerUser.setPassword(MD5Utils.getMD5(pass));
        //产生用户id
        int UID=0;
        //循环产生10001-110001的UID
        Random random=new Random();
        for(int i=0;i<100000;i++)
        {
            int randomID=random.nextInt(100000)+10001;//随机产生用户ID
            User u=userDao.selectUser(Integer.toString(randomID));
            if(u==null)
            {
                UID=randomID;
                registerUser.setUid(Integer.toString(UID));
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

    //修改密码
    public int updatePassword(User user)
    {
        //管理员修改密码
        if(user.getUsername().equals("admin"))
        {
            if(user.getPassword()!=null)
            {
                if(userDao.updatePassword(user))
                    return 1;//修改成功
                else return 0;//失败
            }
            else return -1;//修改失败，密码为空
        }
        else //普通用户修改密码
        {
            if(user.getPassword()!=null)
            {
                String pass=user.getPassword();//明文密码
                String md5_pass=MD5Utils.getMD5(pass);//加密后的密码
                user.setPassword(md5_pass);
                if(userDao.updatePassword(user))
                    return 1;//成功
                else return 0;//失败
            }
            else return -1;//修改失败，密码为空
        }
    }

}
