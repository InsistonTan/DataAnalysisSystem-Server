package com.Tan.service.DataAnalysis;

import com.Tan.domain.InputData;
import com.Tan.utils.mathUtils;
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
 * class:频率性分析service
 * author:TanJifeng
 * last-update:2020-6-07
 * */
@Service
public class FrequenciesService {

    //二项分布检验
    public Map binomialTest(List<InputData> dataList)
    {
        RConnection rc=null;//Rserve连接
        Map result=new HashMap();//返回前端的结果

        //
        if(dataList==null||dataList.size()==0)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:Data length is 0");
            return result;
        }

        //用 R进行二项分布检验---------------------------------------------------------
        try{
            rc=new RConnection();
            //循环对每个变量进行二项分布检验
            for(InputData item:dataList)
            {
                //该变量的检验结果
                Map item_map=new HashMap();
                //存放该变量的数据项名称，第一项是测试值，第二项是测试总数，之后是测试的各个项的出现次数
                String[] names=item.getHeads();
                double[] data=item.getData();
                //
                double test_value=data[0];//测试值（成功的概率）
                double sum=data[1];//测试的总数
                //循环对变量的每一项进行检验
                for(int i=2;i<names.length;i++)
                {
                    //该项的结果
                    Map temp=new HashMap();
                    //获得 R 处理后的结果
                    RList rList=rc.eval("binom.test(x="+data[i]+",n="+sum+",p="+test_value+")").asList();
                    //检验的 P 值
                    double p_value=rList.at(2).asDouble();
                    temp.put("p-value",p_value);
                    //检验的 95%置信区间
                    double[] interval=rList.at(3).asDoubles();
                    String interval_str="[ "+interval[0]+" , "+interval[1]+" ]";
                    temp.put("interval",interval_str);
                    //其他的简单结果
                    temp.put("counts",data[i]);
                    temp.put("total",sum);
                    temp.put("proportion",data[i]/sum);
                    //
                    item_map.put(names[i],temp);
                }
                result.put(item.getHead(),item_map);
            }
        }
        catch (RserveException e)
        {
            if(rc!=null)
                rc.close();
            result.put("statu","failed");
            result.put("msg","Data processing error!\nErrorMsg:"+e.getMessage());
            e.printStackTrace();
            return result;
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

        rc.close();
        result.put("statu","success");
        System.out.println(result);
        return result;
    }

    //对数线性回归
    public Map log_linearRegression(List<InputData> dataList)
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
            //获得因变量
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

            //处理ANOVA-----------------------------------------------------------------
            //获得结果
            rc.voidEval("library(MASS)");
            RList rList=rc.eval("summary(aov(loglm(formula="+formula+")))").asList();
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

    //列联表
    public Map contingencyTables(List<InputData> dataList)
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
        else if(dataList.size()!=2)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:变量个数必须为2个！");
            return result;
        }
        else if(dataList.get(0).getStrData().length!=dataList.get(1).getStrData().length)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:两个变量的数据长度不一致！");
            return result;
        }

        try{
            rc=new RConnection();
            //在R中声明变量
            InputData var_x=dataList.get(0);
            InputData var_y=dataList.get(1);
            rc.assign(var_x.getHead(),var_x.getStrData());
            rc.assign(var_y.getHead(),var_y.getStrData());
            //在R中处理
            RList rList=rc.eval("gmodels::CrossTable("+var_x.getHead()+","+var_y.getHead()
                    +",prop.chisq=FALSE,prop.r=F, prop.c=F,prop.t=F,chisq=T)").asList();
            //处理频数列表--------------------------------------------------------------------------
            Map frequency_map=new HashMap();//存放频数结果
            int[] frequency_data=rList.at(0).asIntegers();//频数数组
            String[] x_names=rList.at(0)._attr().asList().at(1).asList().at(0).asStrings();
            String[] y_names=rList.at(0)._attr().asList().at(1).asList().at(1).asStrings();
            //存放var_x频数结果
            Map x_map=new HashMap();
            for(int i=0;i<x_names.length;i++)
            {
                double[] num=new double[y_names.length+1];//多的一个是用来存放total的值（该行的总数）
                for(int j=0;j<y_names.length;j++)
                {
                    num[j]=frequency_data[i+j*x_names.length];
                }
                num[y_names.length]=mathUtils.getSum(num);//该行的总数
                //
                x_map.put(x_names[i],num);
            }
            //处理每列的总数
            double[] col_sum_list=new double[y_names.length+1];
            for(int i=0;i<y_names.length;i++)
            {
                double col_sum=0;
                for(int j=0;j<x_names.length;j++)
                {
                    col_sum+=frequency_data[i*x_names.length+j];
                }
                col_sum_list[i]=col_sum;
            }
            col_sum_list[y_names.length]=mathUtils.getSum(col_sum_list);
            x_map.put("Total",col_sum_list);
            //
            frequency_map.put(var_x.getHead(),x_map);
            //存放var_y的结果
            frequency_map.put(var_y.getHead(),y_names);
            //
            result.put("frequency",frequency_map);
            //处理卡方检验的结果
            Map  chiSquared=new HashMap();
            double chi=rList.at(4).asList().at(0).asDouble();
            chiSquared.put("Chi^2",chi);
            int df=rList.at(4).asList().at(1).asInteger();
            chiSquared.put("d.f.",df);
            double p_value=rList.at(4).asList().at(2).asDouble();
            chiSquared.put("p_value",p_value);
            //
            result.put("chiSquared",chiSquared);
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

    //贝叶斯列联表
    public Map bayesContingencyTables(List<InputData> dataList,String sampleType,String fixedMargin)
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
        else if(dataList.size()!=2)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:变量个数必须为2个！");
            return result;
        }
        else if(dataList.get(0).getStrData().length!=dataList.get(1).getStrData().length)
        {
            result.put("statu","failed");
            result.put("msg","ErrorMsg:两个变量的数据长度不一致！");
            return result;
        }

        try{
            rc=new RConnection();
            //在R中声明变量
            InputData var_x=dataList.get(0);
            InputData var_y=dataList.get(1);
            rc.assign(var_x.getHead(),var_x.getStrData());
            rc.assign(var_y.getHead(),var_y.getStrData());
            //在R中处理
            RList rList=rc.eval("gmodels::CrossTable("+var_x.getHead()+","+var_y.getHead()
                    +",prop.chisq=FALSE,prop.r=F, prop.c=F,prop.t=F,chisq=T)").asList();
            //处理频数列表--------------------------------------------------------------------------
            Map frequency_map=new HashMap();//存放频数结果
            int[] frequency_data=rList.at(0).asIntegers();//频数数组
            String[] x_names=rList.at(0)._attr().asList().at(1).asList().at(0).asStrings();
            String[] y_names=rList.at(0)._attr().asList().at(1).asList().at(1).asStrings();
            //存放var_x频数结果
            Map x_map=new HashMap();
            for(int i=0;i<x_names.length;i++)
            {
                double[] num=new double[y_names.length+1];//多的一个是用来存放total的值（该行的总数）
                for(int j=0;j<y_names.length;j++)
                {
                    num[j]=frequency_data[i+j*x_names.length];
                }
                num[y_names.length]=mathUtils.getSum(num);//该行的总数
                //
                x_map.put(x_names[i],num);
            }
            //处理每列的总数
            double[] col_sum_list=new double[y_names.length+1];
            for(int i=0;i<y_names.length;i++)
            {
                double col_sum=0;
                for(int j=0;j<x_names.length;j++)
                {
                    col_sum+=frequency_data[i*x_names.length+j];
                }
                col_sum_list[i]=col_sum;
            }
            col_sum_list[y_names.length]=mathUtils.getSum(col_sum_list);
            x_map.put("Total",col_sum_list);
            //
            frequency_map.put(var_x.getHead(),x_map);
            //存放var_y的结果
            frequency_map.put(var_y.getHead(),y_names);
            //
            result.put("frequency",frequency_map);

            //处理贝叶斯的结果----------------------------------------------------------------
            rc.assign("data",frequency_data);//声明频数，生成矩阵
            rc.voidEval("m<-matrix(data=data,nrow="+x_names.length+",ncol="+y_names.length+",byrow = FALSE,dimnames = NULL)");
            RList blist=null;
            if("indepMulti".equals(sampleType))
                blist=rc.eval("BayesFactor::extractBF(BayesFactor::contingencyTableBF(m,sampleType='indepMulti',fixedMargin='"+fixedMargin+"'))").asList();
            else
                blist=rc.eval("BayesFactor::extractBF(BayesFactor::contingencyTableBF(m,sampleType='"+sampleType+"'))").asList();
            double bf=blist.at("bf").asDouble();
            double error=blist.at("error").asDouble();
            result.put("bf",bf);
            result.put("error",error);
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
}
