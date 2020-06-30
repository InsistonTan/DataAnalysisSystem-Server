package com.Tan.service.DataAnalysis;

import com.Tan.domain.InputData;
import com.Tan.utils.mathUtils;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* class:因素分析service
* author:TanJifeng
* last-update:2020-6-11
* */
@Service
public class FactorService {

    //主成分分析
    public Map PrincipalComponentAnalysis(List<InputData> dataList,String path)
    {
        //连接RServe
        RConnection rc=null;

        //存放处理之后的结果(用于返回给前端)
        Map result=new HashMap();

        //
        if(dataList==null||dataList.size()==0)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:Data length is 0");
            return result;
        }
        int len=dataList.get(0).getData().length;
        for(InputData item:dataList)
        {
            if(item.getData().length!=len)
            {
                result.put("statu","failed");
                result.put("msg","ErrorMsg:The data length of the variable:"+item.getHead()+" is inconsistent with other variables！");
                return result;
            }
        }

        try{
            rc=new RConnection();
            //在R中声明变量,并获得数据帧的公式
            StringBuilder data_frame=new StringBuilder("data <- data.frame(");
            for(int i=0;i<dataList.size();i++)
            {
                InputData item=dataList.get(i);
                rc.assign(item.getHead(),item.getData());//声明变量
                if(i==(dataList.size()-1))
                    data_frame.append(item.getHead()+")");
                else
                    data_frame.append(item.getHead()+",");
            }
            rc.voidEval(data_frame.toString());//声明数据帧
            //R处理获得结果------------------------------------------------------
            //生成平行分析碎石图
            String filename=String.valueOf(new Date().getTime());
            rc.voidEval("setwd('"+path+"')");
            rc.voidEval("Cairo::CairoPNG(file = '"+filename+".png')");
            rc.voidEval("psych::fa.parallel(data,n.obs="+len+",fa='pc',n.iter=100,main='Scree plots with parallel analysis')");
            rc.voidEval("dev.off()");
            result.put("parallelPic","/static/save_pics/"+filename+".png");
            RList rList=rc.eval("summary(princomp(data,cor=TRUE))").asList();
            Map importance=new HashMap();//成分重要性结果
            //获得成分名称数组
            String[] cnames=rList.at("sdev")._attr().asList().at(0).asStrings();
            importance.put("cnames",cnames);
            //获得标准差
            double[] sdev=rList.at("sdev").asDoubles();
            importance.put("sdev",sdev);
            //方差数组，即标准差的平方
            double[] dev=new double[sdev.length];
            for(int i=0;i<sdev.length;i++)
            {
                dev[i]=sdev[i]*sdev[i];//标准差的平方为方差
            }
            double dev_sum= mathUtils.getSum(dev);//方差总和
            double[] pov=new double[dev.length];//方差比例数组(每个成分占总方差的比例)
            for(int i=0;i<dev.length;i++)
            {
                pov[i]=dev[i]/dev_sum;
            }
            importance.put("pov",pov);//方差占比
            //累计方差比例
            double[] cp=new double[pov.length];
            for(int i=0;i<pov.length;i++)
            {
                double pov_sum=0;
                for(int j=0;j<=i;j++)
                {
                    pov_sum+=pov[j];
                }
                cp[i]=pov_sum;
            }
            importance.put("cp",cp);//累计方差占比
            //Standardized loadings数据
            Map loading_map=new HashMap();
            double[] loading_data=rList.at("loadings").asDoubles();
            String[] var_names=rList.at("loadings")._attr().asList().at(1).asList().at(0).asStrings();
            for(int i=0;i<var_names.length;i++)
            {
                double[] temp_data=new double[cnames.length];
                for(int j=0;j<cnames.length;j++)
                {
                    temp_data[j]=loading_data[i+j*var_names.length];
                }
                loading_map.put(var_names[i],temp_data);
            }
            importance.put("loadings",loading_map);
            result.put("importance",importance);
            //
            rc.close();
            System.out.println(result);
        }
        catch (REngineException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","Data processing error!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }
        catch (REXPMismatchException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","Data processing error!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }

        result.put("statu","success");
        return result;
    }

    //探索性因子分析
    public Map ExploratoryFactorAnalysis(List<InputData> dataList,double nfactor,String path)
    {
        //连接RServe
        RConnection rc=null;

        //存放处理之后的结果(用于返回给前端)
        Map result=new HashMap();

        //
        if(dataList==null||dataList.size()==0)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:Data length is 0");
            return result;
        }
        int len=dataList.get(0).getData().length;
        for(InputData item:dataList)
        {
            if(item.getData().length!=len)
            {
                result.put("statu","failed");
                result.put("msg","ErrorMsg:The data length of the variable:"+item.getHead()+" is inconsistent with other variables！");
                return result;
            }
        }

        try{
            rc=new RConnection();
            //在R中声明变量,并获得数据框的声明式子
            StringBuilder data_frame=new StringBuilder("data <- data.frame(");
            for(int i=0;i<dataList.size();i++)
            {
                InputData item=dataList.get(i);
                rc.assign(item.getHead(),item.getData());//声明变量
                if(i==(dataList.size()-1))
                    data_frame.append(item.getHead()+")");
                else
                    data_frame.append(item.getHead()+",");
            }
            rc.voidEval(data_frame.toString());//声明数据框

            //R处理获得结果---------------------------------------------------------------
            rc.voidEval("library(psych)");
            //生成平行分析碎石图
            String filename=String.valueOf(new Date().getTime());
            rc.voidEval("setwd('"+path+"')");
            rc.voidEval("Cairo::CairoPNG(file = '"+filename+".png')");
            rc.voidEval("psych::fa.parallel(data,n.obs="+len+",fa='both',n.iter=100,main='Scree plots with parallel analysis')");
            rc.voidEval("dev.off()");
            result.put("parallelPic","/static/save_pics/"+filename+".png");
            //因子分析
            RList rList=rc.eval("fa(data,nfactors="+nfactor+",rotate='none',fm ='pa')").asList();

            //处理loadings数据------------------------------------------------------------
            Map loadings=new HashMap();//成分重要性结果
            String[] var_names=rList.at("loadings")._attr().asList().at(1).asList().at(0).asStrings();
            for(int i=0;i<var_names.length;i++)
            {
                //存放单个变量的结果
                Map temp=new HashMap();
                //处理PA数据
                double[] pa_data=rList.at("loadings").asDoubles();
                String[] pa_names=rList.at("loadings")._attr().asList().at(1).asList().at(1).asStrings();
                for(int j=0;j<pa_names.length;j++)
                {
                    temp.put(pa_names[j],pa_data[i+j*var_names.length]);
                }
                //处理 h2数据
                double[] h2_data=rList.at("communality").asDoubles();
                temp.put("h2",h2_data[i]);
                //处理 u2数据
                double[] u2_data=rList.at("uniquenesses").asDoubles();
                temp.put("u2",u2_data[i]);
                //处理 com数据
                double[] com_data=rList.at("complexity").asDoubles();
                temp.put("com",com_data[i]);
                //loadings的项的名称
                if(i==var_names.length-1)
                {
                    String[] loadingNames=new String[pa_names.length+3];
                    for(int k=0;k<pa_names.length;k++)
                    {
                        loadingNames[k]=pa_names[k];
                    }
                    loadingNames[pa_names.length]="h2";
                    loadingNames[pa_names.length+1]="u2";
                    loadingNames[pa_names.length+2]="com";
                    //
                    loadings.put("loadingNames",loadingNames);
                }

                //
                loadings.put(var_names[i],temp);
            }
            result.put("loadings",loadings);

            //处理 Vaccounted数据
            Map unknow=new HashMap();
            double[] un_data=rList.at("Vaccounted").asDoubles();
            String[] un_varNames=rList.at("Vaccounted")._attr().asList().at(1).asList().at(0).asStrings();
            String[] un_item_names=rList.at("Vaccounted")._attr().asList().at(1).asList().at(1).asStrings();
            for(int i=0;i<un_varNames.length;i++)
            {
                Map temp=new HashMap();
                for(int j=0;j<un_item_names.length;j++)
                {
                    temp.put(un_item_names[j],un_data[i+j*un_varNames.length]);
                }
                unknow.put(un_varNames[i],temp);
            }
            unknow.put("unknowNames",un_item_names);
            //
            result.put("unknow",unknow);
            //
            rc.close();
            System.out.println(result);
        }
        catch (REngineException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","Data processing error!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }
        catch (REXPMismatchException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","Data processing error!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
        }

        result.put("statu","success");
        return result;
    }
}
