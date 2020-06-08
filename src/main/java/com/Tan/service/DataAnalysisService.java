package com.Tan.service;

import com.Tan.domain.InputData;
import com.Tan.utils.mathUtils;
import org.rosuda.REngine.REXP;
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

@Service
public class DataAnalysisService {

    //线性回归处理
    public Map linearRegression(List<InputData> dataList)
    {
        //连接RServe
        RConnection rc=null;

        //存放处理之后的结果(用于返回给前端)
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

            //在R中声明变量
            for(InputData item:dataList)
            {
                rc.assign(item.getHead(),item.getData());
            }
            //处理得到回归关系式，如 y~a+b+...+z
            StringBuilder formula=new StringBuilder();
            InputData dependent = new InputData();
            for(InputData item:dataList)
            {
                if(item.getType().equals("dependent"))
                {
                    dependent.setData(item.getData());
                    dependent.setHead(item.getHead());
                    dependent.setType(item.getType());
                    dataList.remove(item);
                }
                break;
            }
            formula.append(dependent.getHead()+"~");
            for(int i=0;i<dataList.size();i++)
            {
                if(i!=(dataList.size()-1))
                    formula.append(dataList.get(i).getHead()+"+");
                else
                    formula.append(dataList.get(i).getHead());
            }
            //得到线性回归模型
            rc.voidEval("relation <- lm("+formula+")");
            //获得模型参数
            RList result_List=rc.eval("summary(relation)").asList();


            //处理coefficients数据----------------------------------------------------
            Map coefficients=new HashMap();
            //获得协变量的名称字符串数组
            String[] varName=result_List.at("coefficients")._attr().asList().at("dimnames").asList().at(0).asStrings();
            //获得每项的名称
            String[] itemName=result_List.at("coefficients")._attr().asList().at("dimnames").asList().at(1).asStrings();
            //获得值
            double[] coefficients_value=result_List.at("coefficients").asDoubles();
            //循环处理有结果的协变量的数据
            for(int i=0;i<varName.length;i++)
            {
                Map temp =new HashMap();
                for(int j=0;j<itemName.length;j++)
                {
                    temp.put(itemName[j],coefficients_value[i+j*varName.length]);
                }
                coefficients.put(varName[i],temp);
            }
            //处理那些没有结果的协变量
            for(InputData item:dataList)
            {
                boolean hasResult=false;//判断该协变量有没有结果
                for(String str:varName)
                {
                    if(str.equals(item.getHead()))
                    {
                        hasResult=true;
                        break;
                    }
                }
                //该协变量没有结果
                if(hasResult==false)
                {
                    Map temp =new HashMap();
                    for(int j=0;j<itemName.length;j++)
                    {
                        temp.put(itemName[j],"NaN");
                    }
                    coefficients.put(item.getHead(),temp);
                }
            }
            result.put("coefficients",coefficients);

            //处理Residuals数据--------------------------------------------------------
            double[] residuals_value=result_List.at("residuals").asDoubles();
            Map residuals=new HashMap();
            residuals.put("Max", mathUtils.getMax(residuals_value));
            residuals.put("Min",mathUtils.getMin(residuals_value));
            residuals.put("Median",mathUtils.getMedian(residuals_value));
            if(residuals_value.length>=4)
            {
                residuals.put("1Q",mathUtils.get1Q(residuals_value));
                residuals.put("3Q",mathUtils.get3Q(residuals_value));
            }
            else
            {
                residuals.put("1Q","NaN");
                residuals.put("3Q","NaN");
            }
            result.put("residuals",residuals);

            //处理Residual standard error----------------------------------------------
            double sigma=result_List.at("sigma").asDouble();
            result.put("Residual standard error",sigma);

            //处理Multiple R-squared---------------------------------------------------
            double r_squared=result_List.at("r.squared").asDouble();
            result.put("Multiple R-squared",r_squared);

            //处理Adjusted R-squared---------------------------------------------------
            double adj_r_squared=result_List.at("adj.r.squared").asDouble();
            result.put("Adjusted R-squared",adj_r_squared);

            //处理F-statistic----------------------------------------------------------
            REXP fstat_rexp=result_List.at("fstatistic");
            if(fstat_rexp!=null)
            {
                double[] fstatistic_value=fstat_rexp.asDoubles();
                String fstatistic=fstatistic_value[0]+" on "+fstatistic_value[1]+" and "+fstatistic_value[2]+" DF";
                result.put("F-statistic",fstatistic);
            }
            else result.put("F-statistic","NaN");
            //
            rc.close();
            System.out.println(result);
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

    //逻辑回归处理
    public Map logisticRegression(List<InputData> dataList)
    {
        //连接RServe
        RConnection rc=null;

        //存放处理之后的结果(用于返回给前端)
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
            //在R中声明变量
            for(InputData item:dataList)
            {
                rc.assign(item.getHead(),item.getData());
            }
            //处理得到回归关系式，如 y~a+b+...+z
            StringBuilder formula=new StringBuilder();
            InputData dependent = new InputData();
            for(InputData item:dataList)
            {
                if(item.getType().equals("dependent"))
                {
                    dependent.setData(item.getData());
                    dependent.setHead(item.getHead());
                    dependent.setType(item.getType());
                    dataList.remove(item);
                    break;
                }
            }
            formula.append(dependent.getHead()+"~");
            for(int i=0;i<dataList.size();i++)
            {
                if(i!=(dataList.size()-1))
                    formula.append(dataList.get(i).getHead()+"+");
                else
                    formula.append(dataList.get(i).getHead());
            }
            System.out.println(formula);
            //得到线性回归模型
            rc.voidEval("relation <- glm(formula="+formula+",family = binomial)");
            //获得模型参数
            RList result_List=rc.eval("summary(relation)").asList();


            //处理coefficients数据----------------------------------------------------
            Map coefficients=new HashMap();
            //获得协变量的名称字符串数组
            String[] varName=result_List.at("coefficients")._attr().asList().at("dimnames").asList().at(0).asStrings();
            //获得每项的名称
            String[] itemName=result_List.at("coefficients")._attr().asList().at("dimnames").asList().at(1).asStrings();
            //获得值
            double[] coefficients_value=result_List.at("coefficients").asDoubles();
            //循环处理有结果的协变量的数据
            for(int i=0;i<varName.length;i++)
            {
                Map temp =new HashMap();
                for(int j=0;j<itemName.length;j++)
                {
                    temp.put(itemName[j],coefficients_value[i+j*varName.length]);
                }
                coefficients.put(varName[i],temp);
            }
            //处理那些没有结果的协变量
            for(InputData item:dataList)
            {
                boolean hasResult=false;//判断该协变量有没有结果
                for(String str:varName)
                {
                    if(str.equals(item.getHead()))
                    {
                        hasResult=true;
                        break;
                    }
                }
                //该协变量没有结果
                if(hasResult==false)
                {
                    Map temp =new HashMap();
                    for(int j=0;j<itemName.length;j++)
                    {
                        temp.put(itemName[j],"NaN");
                    }
                    coefficients.put(item.getHead(),temp);
                }
            }
            result.put("coefficients",coefficients);

            //处理Deviance.resid数据--------------------------------------------------------
            double[] residuals_value=result_List.at("deviance.resid").asDoubles();
            Map residuals=new HashMap();
            residuals.put("Max", mathUtils.getMax(residuals_value));
            residuals.put("Min",mathUtils.getMin(residuals_value));
            residuals.put("Median",mathUtils.getMedian(residuals_value));
            if(residuals_value.length>=4)
            {
                residuals.put("1Q",mathUtils.get1Q(residuals_value));
                residuals.put("3Q",mathUtils.get3Q(residuals_value));
            }
            else
            {
                residuals.put("1Q","NaN");
                residuals.put("3Q","NaN");
            }
            result.put("deviance.resid",residuals);

            //处理 Null deviance------------------------------------------------------
            double n_dev=result_List.at("null.deviance").asDouble();
            double df_null=result_List.at("df.null").asDouble();
            String null_dev=n_dev+"  on "+df_null+"  degrees of freedom";
            result.put("Null deviance",null_dev);

            //处理 Residual deviance---------------------------------------------------
            double deviance=result_List.at("deviance").asDouble();
            double df_residual=result_List.at("df.residual").asDouble();
            String residual_dev=deviance+"  on "+df_residual+"  degrees of freedom";
            result.put("Residual deviance",residual_dev);

            //处理 AIC
            double aic=result_List.at("aic").asDouble();
            result.put("AIC",aic);

            //处理Number of Fisher Scoring iterations: {{iter : double}}
            double iter=result_List.at("iter").asDouble();
            result.put("iter",iter);

            //
            rc.close();
            System.out.println(result);
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

    //相关性分析
    public Map correlation(List<InputData> dataList)
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

        //获得Rserve连接
        RConnection rc=null;

        try {
            rc=new RConnection();
            //处理数据----------------------------------------------------
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
                double[] temp=dataList.get(i).getData();
                //将temp复制到inputData
                System.arraycopy(temp,0,inputData,inputData_len,temp.length);
                inputData_len+=temp.length;
            }

            //在 R 声明矩阵-----------------------------------------------
            rc.assign("data",inputData);//声明 data
            rc.assign("colnames",col_names);//声明 colnames
            int col_num=dataList.size();//矩阵的列数
            int row_num=dataList.get(0).getData().length;//矩阵的行数
            //声明矩阵 m
            rc.voidEval("m <- matrix(data,ncol="+col_num+",byrow=FALSE,dimnames=list(c(1:"+row_num+"),colnames))");

            //获得协方差矩阵-----------------------------------------------
            double[] cov=rc.eval("cov(m)").asDoubles();
            Map cov_result=new HashMap();
            //处理数据
            for(int i=0;i<dataList.size();i++)
            {
                double[] temp=new double[dataList.size()];
                for(int j=0;j<dataList.size();j++)
                {
                    temp[j]=cov[i+j*dataList.size()];
                }
                cov_result.put(col_names[i],temp);
            }
            result.put("cov",cov_result);

            //获得 pearson 相关系数----------------------------------------
            double[] pearson=rc.eval("cor(m)").asDoubles();
            Map pearson_result=new HashMap();
            //处理数据
            for(int i=0;i<dataList.size();i++)
            {
                double[] temp=new double[dataList.size()];
                for(int j=0;j<dataList.size();j++)
                {
                    temp[j]=pearson[i+j*dataList.size()];
                }
                pearson_result.put(col_names[i],temp);
            }
            result.put("pearson",pearson_result);

            //获得 spearman 相关系数----------------------------------------
            double[] spearman=rc.eval("cor(m,method='spearman')").asDoubles();
            Map spearman_result=new HashMap();
            //处理数据
            for(int i=0;i<dataList.size();i++)
            {
                double[] temp=new double[dataList.size()];
                for(int j=0;j<dataList.size();j++)
                {
                    temp[j]=spearman[i+j*dataList.size()];
                }
                spearman_result.put(col_names[i],temp);
            }
            result.put("spearman",spearman_result);

            //获得 kendall 相关系数----------------------------------------
            double[] kendall=rc.eval("cor(m,method='kendall')").asDoubles();
            Map kendall_result=new HashMap();
            //处理数据
            for(int i=0;i<dataList.size();i++)
            {
                double[] temp=new double[dataList.size()];
                for(int j=0;j<dataList.size();j++)
                {
                    temp[j]=kendall[i+j*dataList.size()];
                }
                kendall_result.put(col_names[i],temp);
            }
            result.put("kendall",kendall_result);

            //
            System.out.print(result);
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

    //描述性统计
    public Map descriptiveStatistics(List<InputData> dataList)
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
            rc.voidEval("library(psych)");//添加依赖包 psych
            for(InputData item:dataList)
            {
                rc.assign(item.getHead(),item.getData());//在 R 中声明变量
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
                double[] temp=dataList.get(i).getData();
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
