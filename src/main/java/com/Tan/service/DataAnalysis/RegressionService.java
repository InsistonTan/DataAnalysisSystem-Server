package com.Tan.service.DataAnalysis;

import com.Tan.domain.InputData;
import com.Tan.utils.mathUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * class:回归分析service
 * author:TanJifeng
 * last-update:2020-6-02
 * */
@Service
public class RegressionService {
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

    //泊松回归处理
    public Map poissonRegression(List<InputData> dataList)
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
            rc.voidEval("relation <- glm(formula="+formula+",family = poisson)");
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
    public Map correlation(List<InputData> dataList,String path)
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
            //生成相关系数矩阵图--------------------------------------------
            String filename=String.valueOf(new Date().getTime());
            rc.voidEval("setwd('"+path+"')");
            rc.voidEval("Cairo::CairoPNG(file = '"+filename+".png')");
            rc.voidEval("PerformanceAnalytics::chart.Correlation(m,method='pearson')");
            rc.voidEval("dev.off()");
            result.put("covPic","/static/save_pics/"+filename+".png");
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
}
