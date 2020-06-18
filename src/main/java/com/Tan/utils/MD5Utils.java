package com.Tan.utils;

import org.springframework.util.DigestUtils;

/**
 * @anthor TanJifeng
 * @Description MD5加密解密工具类
 * @date 2020/6/14 14:07
 */
public class MD5Utils {

    //将明文加密
    public static String getMD5(String text)
    {
        String md5= DigestUtils.md5DigestAsHex(text.getBytes());
        return md5;
    }
    //验证明文和密文是否相同
    public static boolean verify(String md5Text,String text)
    {
        if(md5Text.equalsIgnoreCase(getMD5(text)))
            return true;
        else
            return false;
    }
}
