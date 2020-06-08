package com.Tan.test;
import com.Tan.domain.InputData;
import com.Tan.service.DataAnalysisService;
import com.Tan.utils.mathUtils;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.util.*;
/*
* class:测试类
* author:谭积锋
* last-update:2020-05-02
* */

public class test1 {

    public static void main(String[] args) throws REXPMismatchException, REngineException
    {
        //数组x，y
        double[] data_x={151, 174, 138, 186, 128, 136, 179, 163, 152, 131};
        double[] data_y={63, 81, 56, 91, 47, 57, 76, 72, 62, 48};
        double[] data_z={10,20,30,40,50,60,70,80,90,100};
        double[] x={151, 174, 138, 186, 128, 136, 179, 163, 152, 131};
        double[] y={5,8,11,14,17,20,23,26,29,32};
        InputData x_input=new InputData();
        //x_input.setType("covariate");
        x_input.setHead("data_x");
        x_input.setData(x);
        InputData y_input=new InputData();
        //y_input.setType("dependent");
        y_input.setHead("data_y");
        y_input.setData(y);
        List<InputData> test=new ArrayList<>();
        test.add(x_input);
        test.add(y_input);
        //new DataAnalysisService().reliabilityAnalysis(test);
        new test1().test_ANOVA();
    }

    public void test_ANOVA()throws REngineException, REXPMismatchException
    {
        RConnection rc=new RConnection();
        rc.voidEval("y <-c(5,8,11,14,17,20,23,26,29,32)");
        rc.voidEval("x <- c(5,8,12,13,17,26,35,17,47,36)");
        rc.voidEval("z <- c(21,26,17,36,74,45,28,9,36,14)");
        rc.voidEval("a <-c(5,8,7,4,6,8,1,2,2,4)");
        rc.voidEval("b <-c(12,15,18,17,16,21,23,35,27,29)\n");
        REXP rexp=rc.eval("summary(manova(cbind(a,b)~x))");
        RList rList=rexp.asList();
        double[] data=rList.at(3).asDoubles();
        String[] itemNames=rList.at(3)._attr().asList().at(1).asList().at(1).asStrings();
        String[] varNames=rList.at(3)._attr().asList().at(1).asList().at(0).asStrings();
        for(String i:itemNames)
            System.out.println(i);
        for(String i:varNames)
            System.out.println(i);
    }

    public void test_T_test() throws REngineException, REXPMismatchException {
        RConnection rc=new RConnection();
        double[] high={134, 146, 104, 119, 124, 161, 107};
        double[] low={70, 118, 101, 85, 107, 132, 94};
        rc.assign("high",high);
        rc.assign("low",low);
        RList rList=rc.eval("t.test(high,mu=0.5)").asList();
        showList(rList);
    }

    public void test_ReliabilityAnalysis() throws RserveException, REXPMismatchException
    {
        RConnection rc=new RConnection();
        rc.voidEval("z <- c(151, 174, 138, 186, 128, 136, 179, 163, 152, 131,5,8,11,14,17,20,23,26,29,32)");
        rc.voidEval("m <- matrix(z, ncol = 2, byrow = FALSE, dimnames = list(c(1:10), c('x','y')))");
        rc.voidEval("library(psych)");
        RList sList=rc.eval("alpha(m,check.keys=TRUE)").asList();
        showList(sList);
    }

    public void test_DescriptiveStatistics()
    {
        //
        RConnection rc = null;
        try {
            //建立R连接
            rc=new RConnection();
            double[] newdata={15,20,16,45,78,458,156,125,263};
            rc.assign("newData",newdata);
            rc.voidEval("library(psych)");
            REXP res=rc.eval("describe(newData)");
            String[] names=res._attr().asList().at(0).asStrings();
            for (String i:names)
                System.out.println(i);
            for(int i=0;i<names.length;i++)
            {
                System.out.println(res.asList().at(i).asDouble());
            }
            rc.close();
        } catch (RserveException e) {e.printStackTrace();}
        catch (REngineException e) {e.printStackTrace(); }
        catch (REXPMismatchException e) {e.printStackTrace();}
    }

    public void testCorrelation()
    {
        //
        RConnection rc = null;
        try {
            //建立R连接
            rc=new RConnection();
            //数组x，y
            /*double[] data_x={84,56,47,89,53};
            double[] data_y={0.1,0.42,0.33,0.24,0.15};
            double[] newdata = new double[data_x.length+data_y.length];
            System.arraycopy(data_x,0,newdata,0,data_x.length);
            System.arraycopy(data_y,0,newdata,data_x.length,data_y.length);*/
            double[] newdata={1,2,3,4,5,2,4,6,8,10,5,7,8,3,6};
            //
            String[] colnames={"data_x","data_y","data_z"};
            //
            rc.assign("data",newdata);//在R中声明变量data,赋值
            rc.assign("colnames",colnames);
            rc.voidEval("m <- matrix(data,ncol=3,byrow=FALSE,dimnames=list(c(1:5),colnames))");
            //相关性
            double[] sList= rc.eval("cor(m)").asDoubles();
            for(double i:sList)
                System.out.print(i);

            rc.close();
        } catch (RserveException e) {e.printStackTrace();}
        catch (REngineException e) {e.printStackTrace(); }
        catch (REXPMismatchException e) {e.printStackTrace();}
    }

    public void test_LogisticRegression() throws REXPMismatchException, REngineException
    {
        //
        RConnection rc = null;
        try {
            //建立R连接
            rc=new RConnection();
            //数组x，y
            double[] data_x={84,56,47,89,53};
            double[] data_y={0.1,0.42,0.33,0.24,0.15};
            rc.assign("x",data_x);//在R中声明变量x,赋值
            rc.assign("y",data_y);//在R中声明变量y,赋值
            //线性回归
            rc.voidEval("relation <- glm(y~x,family = binomial)");//建立模型
            RList sList= rc.eval("summary(relation)").asList();//得到模型参数
            String[] keyStrs=sList.keys();//保存模型中的参数名
            System.out.println("包含的项有：");
            //循环输出所有数据（double型）
            for(String name:keyStrs)
            {
                System.out.println(String.format("----------------------"));
                System.out.println(name+":");
                REXP rexp=sList.at(name);//获取该参数名的数据
                //输出double类型的数据
                try {
                    double[] data = rexp.asDoubles();//得到数据
                    for (double i : data)//输出
                        System.out.println(String.format("%.4f", i));
                }
                catch (REXPMismatchException e){
                    //continue;
                }
            }
            rc.close();
        } catch (RserveException e) {e.printStackTrace();}
        catch (REngineException e) {e.printStackTrace(); }
        catch (REXPMismatchException e) {e.printStackTrace();}

    }

    public void test_N_linearRegression()
    {
        //
        RConnection rc = null;
        try {
            //建立R连接
            rc = new RConnection();
            //数组x，y
            /*double[] data_x = {151, 174, 138, 186, 128, 136, 179, 163, 152, 131};
            double[] data_y = {63, 81, 56, 91, 47, 57, 76, 72, 62, 48};
            double[] data_z= {50,70,43,82,45,53,67,69,51,39};*/
            double[] data_y = {5,8,11,14,17,20,23,26,29,32};
            double[] data_x = {1,2,3,4,5,6,7,8,9,10};
            double[] data_z= {2,3,4,5,6,7,8,9,10,11};
            rc.assign("x", data_x);//在R中声明变量x,赋值
            rc.assign("y", data_y);//在R中声明变量y,赋值
            rc.assign("z", data_z);//在R中声明变量z,赋值
            //线性回归
            rc.voidEval("model <- lm(y~x+z)");//建立模型
            RList sList = rc.eval("summary(model)").asList();//得到模型参数
            //
            String[] a=sList.at("coefficients")._attr().asList().at("dimnames").asList().at(0).asStrings();
            String[] b=sList.at("coefficients")._attr().asList().at("dimnames").asList().at(1).asStrings();
            /*for(String i:keys)
                System.out.println(i);*/
            for(String i:a)
                System.out.println(i);
            for(String i:b)
                System.out.println(i);
            //
            /*String[] keyStrs = sList.keys();//保存模型中的参数名
            System.out.println("包含的项有：");
            //循环输出所有数据（double型）
            for (String name : keyStrs)
            {
                System.out.println(String.format("----------------------"));
                System.out.println(name + ":");
                REXP rexp = sList.at(name);//获取该参数名的数据
                //输出double类型的数据
                try {
                    double[] data = rexp.asDoubles();//得到数据
                    for (double i : data)//输出
                        System.out.println(String.format("%.4f", i));
                } catch (REXPMismatchException e) {
                    //continue;
                }
            }*/
        } catch (RserveException e) {e.printStackTrace();} catch (REngineException e) {e.printStackTrace();
        } catch (REXPMismatchException e) {e.printStackTrace();}
    }

    public void test_linearRegression() throws REXPMismatchException, REngineException
    {
        //
        RConnection rc = null;
        try {
            //建立R连接
            rc=new RConnection();
            //数组x，y
            int[] data_x={151, 174, 138, 186, 128, 136, 179, 163, 152, 131};
            int[] data_y={63, 81, 56, 91, 47, 57, 76, 72, 62, 48};
            rc.assign("x",data_x);//在R中声明变量x,赋值
            rc.assign("y",data_y);//在R中声明变量y,赋值
            //线性回归
            rc.voidEval("relation <- lm(y~x)");//建立模型
            RList sList= rc.eval("summary(relation)").asList();//得到模型参数
            String[] keyStrs=sList.keys();//保存模型中的参数名
            System.out.println("包含的项有：");
            //循环输出所有数据（double型）
            for(String name:keyStrs)
            {
                System.out.println(String.format("----------------------"));
                System.out.println(name+":");
                REXP rexp=sList.at(name);//获取该参数名的数据
                //输出double类型的数据
                try {
                    double[] data = rexp.asDoubles();//得到数据
                    for (double i : data)//输出
                        System.out.println(String.format("%.4f", i));
                }
                catch (REXPMismatchException e){
                    //continue;
                }

            }
            double[] residuals=sList.at("residuals").asDoubles();
            Arrays.sort(residuals);
            for(double i:residuals)
                System.out.print(i+"\t");
            System.out.println();
            System.out.println("Min:"+mathUtils.getMin(residuals));
            System.out.println("1Q:"+mathUtils.get1Q(residuals));
            System.out.println("Median:"+mathUtils.getMedian(residuals));
            System.out.println("3Q:"+mathUtils.get3Q(residuals));
            System.out.println("Max:"+mathUtils.getMax(residuals));
            rc.close();
        } catch (RserveException e) {e.printStackTrace();}
          catch (REngineException e) {e.printStackTrace(); }
        catch (REXPMismatchException e) {e.printStackTrace();}

    }

    public void showList(RList sList)
    {
        String[] keyStrs = sList.keys();//保存模型中的参数名
            System.out.println("包含的项有：");
            //循环输出所有数据（double型）
            for (String name : keyStrs)
            {
                System.out.println(String.format("----------------------"));
                System.out.println(name + ":");
                REXP rexp = sList.at(name);//获取该参数名的数据
                //输出double类型的数据
                try {
                    double[] data = rexp.asDoubles();//得到数据
                    for (double i : data)//输出
                        System.out.println(String.format("%.4f", i));
                } catch (REXPMismatchException e) {
                    //continue;
                }
            }
    }
}
