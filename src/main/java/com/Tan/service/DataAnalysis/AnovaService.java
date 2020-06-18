package com.Tan.service.DataAnalysis;

import com.Tan.domain.InputData;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
* class:方差分析service
* author:TanJifeng
* last-update:2020-6-08
* */
@Service
public class AnovaService {

    //方差分析（ANOVA）
    public Map anova(List<InputData> dataList)
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

        //处理输入数据（找到因变量）------------------------------------
        InputData dependentVar=null;
        for(InputData item:dataList)
        {
            if("dependent".equals(item.getType()))
            {
                dependentVar=new InputData();
                dependentVar.setType(item.getType());
                dependentVar.setHead(item.getHead());
                dependentVar.setData(item.getData());
                dataList.remove(item);
                break;
            }
        }
        //没有因变量
        if(dependentVar==null)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:没有因变量！");
            return result;
        }

        //用 R进行ANOVA分析------------------------------------------
        try{
            rc=new RConnection();
            //处理得到公式
            StringBuilder formula=new StringBuilder();//公式
            formula.append(dependentVar.getHead()+"~");
            //声明因变量
            rc.assign(dependentVar.getHead(),dependentVar.getData());
            //循环处理公式，声明固定因子
            for(int i=0;i<dataList.size();i++)
            {
                InputData item=dataList.get(i);
                if(i==(dataList.size()-1))
                    formula.append(item.getHead());
                else
                    formula.append(item.getHead()+"*");
                //声明变量
                rc.assign(item.getHead(),item.getData());
            }

            //获得结果
            RList rList=rc.eval("summary(aov("+formula+"))").asList();
            //结果项的名称
            String[] itemNames=rList.at(0)._attr().asList().at(0).asStrings();
            //变量的名称
            String[] varNames=rList.at(0)._attr().asList().at(1).asStrings();
            //存放 anova结果
            Map anova_map=new HashMap();
            for(int i=0;i<varNames.length;i++)
            {
                Map temp=new HashMap();
                for(int j=0;j<itemNames.length;j++)
                {
                    double value=rList.at(0).asList().at(j).asDoubles()[i];
                    temp.put(itemNames[j],value);
                }
                anova_map.put(varNames[i],temp);
            }
            result.put("anova",anova_map);
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

    //协方差分析（ANCOVA）
    public Map ancova(List<InputData> dataList)
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

        //处理输入数据（找到因变量、协变量）------------------------------------
        //找因变量
        InputData dependentVar=null;
        for(InputData item:dataList)
        {
            if("dependent".equals(item.getType()))
            {
                dependentVar=new InputData();
                dependentVar.setType(item.getType());
                dependentVar.setHead(item.getHead());
                dependentVar.setData(item.getData());
                dataList.remove(item);
                break;
            }
        }
        //没有因变量
        if(dependentVar==null)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:没有因变量！");
            return result;
        }
        //找协变量,存放在covarList
        List<InputData> covarList=new ArrayList<>();
        for(int i=0;i<dataList.size();i++)
        {
            InputData item=dataList.get(i);
            if("covar".equals(item.getType()))
            {
                covarList.add(item);
                dataList.remove(item);
                i--;
            }
        }

        //用 R进行ANOVA分析---------------------------------------------------------
        try{
            rc=new RConnection();
            //处理得到公式
            StringBuilder formula=new StringBuilder();//公式
            formula.append(dependentVar.getHead()+"~");
            //声明因变量
            rc.assign(dependentVar.getHead(),dependentVar.getData());
            //循环处理公式的协变量部分，并声明协变量
            for(int j=0;j<covarList.size();j++)
            {
                InputData item=covarList.get(j);
                if(j==covarList.size()-1)
                    formula.append(item.getHead());
                else
                    formula.append(item.getHead()+"+");
                //声明变量
                rc.assign(item.getHead(),item.getData());
            }
            if(covarList.size()>0)
            {
                formula.append("+");
            }
            //循环处理公式的固定因子部分，并声明固定因子
            for(int i=0;i<dataList.size();i++)
            {
                InputData item=dataList.get(i);
                if(i==(dataList.size()-1))
                    formula.append(item.getHead());
                else
                    formula.append(item.getHead()+"*");
                //声明变量
                rc.assign(item.getHead(),item.getData());
            }

            //获得结果--------------------------------------------------------------
            RList rList=rc.eval("summary(aov("+formula+"))").asList();
            //结果项的名称
            String[] itemNames=rList.at(0)._attr().asList().at(0).asStrings();
            //变量的名称
            String[] varNames=rList.at(0)._attr().asList().at(1).asStrings();
            //存放 anova结果
            Map ancova_map=new HashMap();
            for(int i=0;i<varNames.length;i++)
            {
                Map temp=new HashMap();
                for(int j=0;j<itemNames.length;j++)
                {
                    double value=rList.at(0).asList().at(j).asDoubles()[i];
                    temp.put(itemNames[j],value);
                }
                ancova_map.put(varNames[i],temp);
            }
            result.put("ancova",ancova_map);
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

    //多变量方差分析（MANOVA）
    public Map manova(List<InputData> dataList)
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

        //处理输入数据（找到因变量、协变量）------------------------------------
        //找因变量
        List<InputData> depenList=new ArrayList<>();
        for(int i=0;i<dataList.size();i++)
        {
            InputData item=dataList.get(i);
            if("dependent".equals(item.getType()))
            {
                InputData depent=new InputData();
                depent.setHead(item.getHead());
                depent.setData(item.getData());
                depenList.add(item);
                dataList.remove(item);
                i--;
            }
        }
        //因变量个数小于2
        if(depenList.size()<2)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:因变量个数不能小于2个！");
            return result;
        }

        //用 R进行ANOVA分析---------------------------------------------------------
        try{
            rc=new RConnection();
            //处理得到公式
            StringBuilder formula=new StringBuilder();//公式
            formula.append("cbind(");
            //循环处理公式的因变量部分，并声明因变量
            for(int j=0;j<depenList.size();j++)
            {
                InputData item=depenList.get(j);
                if(j==(depenList.size()-1))
                    formula.append(item.getHead()+")~");
                else
                    formula.append(item.getHead()+",");
                //声明变量
                rc.assign(item.getHead(),item.getData());
            }
            //循环处理公式的固定因子部分，并声明固定因子
            for(int i=0;i<dataList.size();i++)
            {
                InputData item=dataList.get(i);
                if(i==(dataList.size()-1))
                    formula.append(item.getHead());
                else
                    formula.append(item.getHead()+"*");
                //声明变量
                rc.assign(item.getHead(),item.getData());
            }

            //获得结果--------------------------------------------------------------
            RList rList=rc.eval("summary(manova("+formula+"))").asList();
            double[] data=rList.at(3).asDoubles();
            //结果项的名称
            String[] itemNames=rList.at(3)._attr().asList().at(1).asList().at(1).asStrings();
            //变量的名称
            String[] varNames=rList.at(3)._attr().asList().at(1).asList().at(0).asStrings();
            //存放 anova结果
            //循环处理得到返回前端的结果数据
            Map manova_map=new HashMap();
            for(int i=0;i<varNames.length;i++)
            {
                Map temp=new HashMap();
                for(int j=0;j<itemNames.length;j++)
                {
                    double value=data[i+j*varNames.length];
                    temp.put(itemNames[j],value);
                }
                manova_map.put(varNames[i],temp);
            }
            result.put("manova",manova_map);
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
