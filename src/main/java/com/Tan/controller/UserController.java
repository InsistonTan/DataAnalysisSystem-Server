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
 * @Author Zhongger
 * @Description 用户登录注册
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
    @RequestMapping(value = "/login")
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
            request.getSession().setAttribute("id",userService.getIdByName(loginUser));
            //设置返回前端的信息
            result.put("code","success");
        }
        else if(check_result==0)
            result.put("code","passwordError");
        else if(check_result==-1)
            result.put("code","usernameIsNotExit");
        return result;
    }

    //注册
    @RequestMapping(value = "/register")
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
            result.put("msg","success");
        else if (check_result==0)
            result.put("msg","fail");
        else if (check_result==-1)
            result.put("msg","usernameIsExit");
        else
            result.put("msg","unknowError");
        return result;
    }

    //清除登陆信息
    @RequestMapping(value = "/clearInfo")
    public void clearInfo()
    {
        request.getSession().removeAttribute("id");
        request.getSession().removeAttribute("username");
    }
}
