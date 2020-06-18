package com.Tan.controller;
import com.Tan.domain.User;
import com.Tan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author 谭积锋
 * @Description 用户登录注册控制器
 * @Date 2020.2.17
 */
@CrossOrigin
@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    HttpServletRequest request;

    //登陆
    @RequestMapping(value = "/api/login")
    @ResponseBody
    public Map<String,String> login(@RequestBody User loginUser)
    {
        Map<String,String> result=new HashMap<String,String>();
        //登陆
        System.out.println("登陆："+loginUser.getUsername()+"\t"+loginUser.getPassword());
        int check_result=userService.loginCheck(loginUser);
        System.out.println("登陆结果："+check_result);
        //登陆成功
        if(check_result==1)
        {
            //设置session
            request.getSession().setMaxInactiveInterval(12*60*60);
            request.getSession().setAttribute("username",loginUser.getUsername());
            String uid=userService.getIdByName(loginUser);
            request.getSession().setAttribute("uid",uid);
            //设置返回前端的信息
            result.put("statu","success");
            result.put("uid",uid);
            result.put("username",loginUser.getUsername());
        }
        else if(check_result==0)
        {
            result.put("statu","failed");
            result.put("msg","密码错误");
        }
        else if(check_result==-1)
        {
            result.put("statu","failed");
            result.put("msg","用户不存在");
        }

        return result;
    }

    //注册
    @RequestMapping(value = "/api/register")
    @ResponseBody
    public Map<String,String> register(@RequestBody User user)
    {
        Map<String,String> result=new HashMap<String,String>();
        //
        System.out.println("注册："+user.getUsername()+"\t"+user.getPassword());
        //注册
        int check_result=userService.registerUser(user);
        System.out.println("注册结果："+check_result);
        //注册成功
        if(check_result==1)
            result.put("statu","success");
        else if (check_result==0)
        {
            result.put("statu","failed");
            result.put("msg","注册失败！");
        }
        else if (check_result==-1)
        {
            result.put("statu","failed");
            result.put("msg", "用户名已存在，请更换用户名");
        }
        else
        {
            result.put("statu","failed");
            result.put("msg","未知错误！");
        }
        return result;
    }

    //检查登陆
    @RequestMapping(value = "/api/checkLogin")
    @ResponseBody
    public Map checkLogin()
    {
        Map user=new HashMap();
        String uid=String.valueOf(request.getSession().getAttribute("uid"));
        String username=String.valueOf(request.getSession().getAttribute("username"));
        if(uid!=null)
        {
            user.put("uid",uid);
            user.put("username",username);
        }
        return user;
    }

    //清除登陆信息
    @RequestMapping(value = "/api/clearInfo")
    public void clearInfo()
    {
        request.getSession().removeAttribute("uid");
        request.getSession().removeAttribute("username");
    }
}
