package com.Tan.service.DataAnalysis;

import com.Tan.domain.InputData;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * class:T检验service
 * author:TanJifeng
 * last-update:2020-6-05
 * */
@Service
public class T_testsService {


    //独立样本 T 检验
    public Map independentSample(List<InputData> dataList)
    {
        RConnection rc=null;//Rserve连接
        Map result=new HashMap();//返回前端的结果

        //
        if(dataList==null||dataList.size()==0)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:数据长度为0");
            return result;
        }

        //处理输入的数据-------------------------------------------
        //找到分组变量
        InputData groupVar=null;//分组变量
        for(InputData item:dataList)
        {
            //找到分组变量
            if("groupVar".equals(item.getType()))
            {
                groupVar=new InputData();
                groupVar.setType(item.getType());
                groupVar.setHead(item.getHead());
                groupVar.setData(item.getData());
                dataList.remove(item);//已经找到分组变量，则将其从dataList移除
                break;
            }
        }
        //没找到分组变量
        if(groupVar==null)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:缺少分组变量");
            return result;
        }

        //用 R进行 T 检验------------------------------------------
        try{
            rc=new RConnection();
            //在 R 中声明分组变量
            rc.assign(groupVar.getHead(),groupVar.getData());

            //循环处理独立样本 T 检验
            for(InputData item:dataList)
            {
                rc.assign(item.getHead(),item.getData());
                RList rList=rc.eval("t.test("+item.getHead()+","+groupVar.getHead()+",paired=FALSE)").asList();
                Map item_res=new HashMap();
                //处理数据
                item_res.put("statistic",rList.at("statistic").asDouble());// t值
                item_res.put("parameter",rList.at("parameter").asDouble());//df值
                item_res.put("p.value",rList.at("p.value").asDouble());//p值
                double[] interval=rList.at("conf.int").asDoubles();
                String interval_95="["+interval[0]+","+interval[1]+"]";//95%的置信区间
                item_res.put("interval",interval_95);
                //添加到result
                result.put(item.getHead(),item_res);
            }

        }
        catch (RserveException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","后台处理数据错误!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }
        catch (REngineException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","后台处理数据错误!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }
        catch (REXPMismatchException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","后台处理数据错误!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }

        rc.close();
        result.put("statu","success");
        System.out.println(result);
        return result;
    }

    //配对样本 T 检验
    public Map pairedSample(List<InputData> dataList)
    {
        RConnection rc=null;//Rserve连接
        Map result=new HashMap();//返回前端的结果

        //
        if(dataList==null||dataList.size()==0)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:数据长度为0");
            return result;
        }

        //处理输入的数据-------------------------------------------
        if ((dataList.size()%2)!=0)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:选择了奇数个变量，无法按序两两配对");
            return result;
        }

        //用 R进行配对 T 检验------------------------------------------
        try{
            rc=new RConnection();

            //循环处理独立样本 T 检验
            for(int i=0;i<dataList.size();i+=2)
            {
                //两个配对的变量
                InputData pairA=dataList.get(i);
                InputData pairB=dataList.get(i+1);
                //R处理
                rc.assign(pairA.getHead(),pairA.getData());
                rc.assign(pairB.getHead(),pairB.getData());
                RList rList=rc.eval("t.test("+pairA.getHead()+","+pairB.getHead()+",paired=TRUE)").asList();
                Map item_res=new HashMap();
                //处理数据
                item_res.put("statistic",rList.at("statistic").asDouble());// t值
                item_res.put("parameter",rList.at("parameter").asDouble());//df值
                item_res.put("p.value",rList.at("p.value").asDouble());//p值
                double[] interval=rList.at("conf.int").asDoubles();
                String interval_95="["+interval[0]+" , "+interval[1]+"]";//95%的置信区间
                item_res.put("interval",interval_95);
                //添加到result
                result.put(pairA.getHead()+"~"+pairB.getHead(),item_res);
            }

        }
        catch (RserveException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","后台处理数据错误!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }
        catch (REngineException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","后台处理数据错误!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }
        catch (REXPMismatchException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","后台处理数据错误!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }

        rc.close();
        result.put("statu","success");
        System.out.println(result);
        return result;
    }

    //单样本 T 检验
    public Map oneSample(double test_value,List<InputData> dataList)
    {
        RConnection rc=null;//Rserve连接
        Map result=new HashMap();//返回前端的结果

        //
        if(dataList==null||dataList.size()==0)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:数据长度为0");
            return result;
        }

        //用 R进行单样本 T 检验------------------------------------------
        try{
            rc=new RConnection();

            //循环处理独立样本 T 检验
            for(InputData item:dataList)
            {
                //R处理
                rc.assign(item.getHead(),item.getData());
                RList rList=rc.eval("t.test("+item.getHead()+",mu="+test_value+")").asList();
                Map item_res=new HashMap();
                //处理数据
                item_res.put("statistic",rList.at("statistic").asDouble());// t值
                item_res.put("parameter",rList.at("parameter").asDouble());//df值
                item_res.put("p.value",rList.at("p.value").asDouble());//p值
                double[] interval=rList.at("conf.int").asDoubles();
                String interval_95="["+interval[0]+","+interval[1]+"]";//95%的置信区间
                item_res.put("interval",interval_95);
                //添加到result
                result.put(item.getHead(),item_res);
            }

        }
        catch (RserveException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","后台处理数据错误!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }
        catch (REngineException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","后台处理数据错误!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }
        catch (REXPMismatchException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","后台处理数据错误!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }

        rc.close();
        result.put("statu","success");
        System.out.println(result);
        return result;
    }
}
