package com.Tan.service.DataAnalysis;

import com.Tan.domain.InputData;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * class:描述性分析service
 * author:TanJifeng
 * last-update:2020-6-03
 * */
@Service
public class DescriptivesService {

    //描述性统计
    public Map descriptiveStatistics(List<InputData> dataList,String path)
    {
        //Rserve连接
        RConnection rc=null;

        //返回前端的结果 Map
        Map result=new HashMap();

        //
        if(dataList==null||dataList.size()==0)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:数据长度为0");
            return result;
        }

        try{
            rc=new RConnection();
            //循环处理基本统计数据
            Map baseDescriptives=new HashMap();//存放基本统计数据
            Map pics=new HashMap();//存放图片url
            rc.voidEval("library(psych)");//添加依赖包 psych
            //int n=0;//图片序号
            for(InputData item:dataList)
            {
                rc.assign(item.getHead(),item.getData());//在 R 中声明变量
                //生成直方图图
                String filename=String.valueOf(new Date().getTime());
                rc.voidEval("setwd('"+path+"')");//设置保存的路径
                rc.voidEval("Cairo::CairoPNG(file = '"+filename+".png')");//文件名
                rc.voidEval("hist("+item.getHead()+",freq=FALSE)");//画图
                rc.voidEval("lines(density("+item.getHead()+"),col='blue')");
                rc.voidEval("dev.off()");//结束
                pics.put(item.getHead(),"/static/save_pics/"+filename+".png");
                //n++;
                //
                REXP resREXP=rc.eval("describe("+item.getHead()+")");//获得基本统计结果
                //结果包含的项的名称
                String[] names=resREXP._attr().asList().at(0).asStrings();
                //单项的结果
                Map temp=new HashMap();
                for(int i=0;i<names.length;i++)
                {
                    double value=resREXP.asList().at(i).asDouble();
                    temp.put(names[i],value);
                }
                baseDescriptives.put(item.getHead(),temp);
            }
            result.put("baseDescriptives",baseDescriptives);
            result.put("pics",pics);
            //
            System.out.println(result);
            rc.close();
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

        result.put("statu","success");
        return result;
    }

    //可靠性分析
    public Map reliabilityAnalysis(List<InputData> dataList)
    {
        //存放返回给前端的结果map
        Map result=new HashMap();

        //
        if(dataList==null||dataList.size()==0)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:数据长度为0");
            return result;
        }

        RConnection rc=null;
        try{
            //获得Rserve连接
            rc=new RConnection();

            //处理输入数据---------------------------------------------------------------------
            //inputData用于在 R 中声明输入数据的矩阵
            double[] inputData=new double[dataList.size()*dataList.get(0).getData().length];
            int inputData_len=0;//当前 inpuData 已有的数据个数（用于从其他数组复制数据到inputData）
            String[] col_names=new String[dataList.size()];//矩阵的列名
            //循环处理数据
            for(int i=0;i<dataList.size();i++)
            {
                //存放列名
                col_names[i]=dataList.get(i).getHead();
                //将 dataList 的所有数组整合到 inputData
                double[] temp= dataList.get(i).getData();
                //将temp复制到inputData
                System.arraycopy(temp,0,inputData,inputData_len,temp.length);
                inputData_len+=temp.length;
            }

            //在 R 声明矩阵---------------------------------------------------------------------------
            rc.assign("data",inputData);//声明 data
            rc.assign("colnames",col_names);//声明 colnames
            int col_num=dataList.size();//矩阵的列数
            int row_num=dataList.get(0).getData().length;//矩阵的行数
            //声明矩阵 m
            rc.voidEval("m <- matrix(data,ncol="+col_num+",byrow=FALSE,dimnames=list(c(1:"+row_num+"),colnames))");

            //获取可靠性分析结果-----------------------------------------------------------------------
            rc.voidEval("library(psych)");//加载依赖包
            //获得结果列表
            RList result_list=rc.eval("alpha(m,check.keys=TRUE)").asList();

            //处理 " total "数据---------------------------------------------------------------------
            RList total_data=result_list.at("total").asList();//total数据
            //数据项的名称
            String[] total_names=result_list.at("total")._attr().asList().at(0).asStrings();
            //存放total的返回前端的结果
            Map total_map=new HashMap();
            for(int i=0;i<total_names.length;i++)
            {
                total_map.put(total_names[i],total_data.at(i).asDouble());
            }
            result.put("total",total_map);

            //处理 alpha.drop 数据--------------------------------------------------------------------
            RList drop_data=result_list.at("alpha.drop").asList();//total数据
            //数据项的名称
            String[] drop_names=result_list.at("alpha.drop")._attr().asList().at(0).asStrings();
            //输入的数据的列名
            String[] drop_varNames=result_list.at("alpha.drop")._attr().asList().at(1).asStrings();
            //存放total的返回前端的结果
            Map drop_map=new HashMap();
            //循环处理返回前端的数据
            for(int i=0;i<drop_varNames.length;i++)
            {
                Map temp=new HashMap();//一行结果（输入数据有多少项就有多少行）
                for(int j=0;j<drop_names.length;j++)
                {
                    double value=drop_data.at(j).asDoubles()[i];
                    temp.put(drop_names[j],value);
                }
                drop_map.put(drop_varNames[i],temp);
            }
            result.put("drop",drop_map);

            //处理 item.stats 数据
            REXP statsREXP=result_list.at("item.stats");
            //total数据
            RList stats_data=statsREXP.asList();
            //数据项的名称
            String[] stats_names=statsREXP._attr().asList().at(0).asStrings();
            //输入的数据的列名
            String[] stats_varNames=statsREXP._attr().asList().at(2).asStrings();
            //存放total的返回前端的结果
            Map stats_map=new HashMap();
            //循环处理返回前端的数据
            for(int i=0;i<stats_varNames.length;i++)
            {
                Map temp=new HashMap();//一行结果（输入数据有多少项就有多少行）
                for(int j=0;j<stats_names.length;j++)
                {
                    double value=stats_data.at(j).asDoubles()[i];
                    temp.put(stats_names[j],value);
                }
                stats_map.put(stats_varNames[i],temp);
            }
            result.put("stats",stats_map);

            //
            System.out.println(result);
            rc.close();
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
        //
        result.put("statu","success");
        return result;
    }
}
