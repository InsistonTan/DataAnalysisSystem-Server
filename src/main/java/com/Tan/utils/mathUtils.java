package com.Tan.utils;

import java.util.Arrays;
import java.util.List;

/*
* class:用于处理一些数学问题的工具类
* */
public class mathUtils {

    //获取数组中最大的值
    public static double getMax(double[] data)
    {
        //排序
        Arrays.sort(data);
        return data[data.length-1];
    }
    //获取数组中最小的值
    public static double getMin(double[] data)
    {
        //排序
        Arrays.sort(data);
        return data[0];
    }
    //获取数组的中位数
    public static double getMedian(double[] data)
    {
        //排序
        Arrays.sort(data);
        int len=data.length;
        if(len%2!=0)//总共有奇数个数
            return data[(len-1)/2];//返回中位数
        else
            return (data[len/2]+data[len/2-1])/2;//返回中间两数的平均
    }
    //获取数组中的从小到大排列后的第25%位置的值
    public static double get1Q(double[] data)
    {
        //排序
        Arrays.sort(data);
        int len=data.length;
        //使用(N+1)法获得第25%位置的下标
        double index1Q=len*0.25;
        //判断下标是否为整数
        if(index1Q-(int)index1Q==0)//下标为整数
            //直接返回第25%位置的值
            return data[(int)(index1Q)-1];
        else
        {
            //获取下标的左右的数值的比例
            double leftPercent=index1Q-(int)index1Q;
            double rightPercent=1-leftPercent;
            return data[(int)(index1Q)-1]*leftPercent+data[(int)(index1Q)]*rightPercent;
        }

    }
    //获取数组中的从小到大排列后的第75%位置的值
    public static double get3Q(double[] data)
    {
        //排序
        Arrays.sort(data);
        int len=data.length;
        //使用(N+1)法获得第25%位置的下标
        double index3Q=len*0.75;
        //判断下标是否为整数
        if(index3Q-(int)index3Q==0)//下标为整数
            //直接返回第25%位置的值
            return data[(int)(index3Q)-1];
        else
        {
            //获取下标的左右的数值的比例
            double leftPercent=index3Q-(int)index3Q;
            double rightPercent=1-leftPercent;
            return data[(int)(index3Q)-1]*leftPercent+data[(int)(index3Q)]*rightPercent;
        }
    }
    //获得数组的数据总和
    public static double getSum(double[] data)
    {
        double sum=0;
        for(double i:data)
            sum+=i;
        return sum;
    }

}
