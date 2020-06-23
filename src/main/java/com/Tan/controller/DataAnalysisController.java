package com.Tan.controller;
import com.Tan.domain.InputData;
import com.Tan.domain.RequestData;
import com.Tan.service.DataAnalysis.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.Map;
/*
 * @Description:数据分析控制器
 * @author:TanJifeng
 * @date:2020-06-01
 * */
@Controller
public class DataAnalysisController {
    @Autowired
    AnovaService anovaService;
    @Autowired
    DescriptivesService descriptivesService;
    @Autowired
    FactorService factorService;
    @Autowired
    FrequenciesService frequenciesService;
    @Autowired
    RegressionService regressionService;
    @Autowired
    T_testsService t_testsService;

    //回归分析
    @RequestMapping(value = "/api/Regression")
    @ResponseBody
    public Map regression(@NotNull @RequestBody RequestData requestData)
    {
        System.out.println(requestData.getAction());
        System.out.println(requestData.getDataList());
        Map result=null;
        //线性回归
        if("linearRegression".equals(requestData.getAction()))
            result=regressionService.linearRegression(requestData.getDataList());
        //逻辑回归
        else if("logisticRegression".equals(requestData.getAction()))
            result=regressionService.logisticRegression(requestData.getDataList());
        //泊松回归
        else if("poissonRegression".equals(requestData.getAction()))
            result=regressionService.poissonRegression(requestData.getDataList());
        //相关性
        else if("correlation".equals(requestData.getAction()))
            result=regressionService.correlation(requestData.getDataList(),getSavePath());
        //贝叶斯线性回归
        else if("bayesLinearRegression".equals(requestData.getAction()))
            result=regressionService.bayesLinearRegression(requestData.getDataList());
        //贝叶斯相关性
        else if("bayesCorrelation".equals(requestData.getAction()))
            result=regressionService.bayesCorrelation(requestData.getDataList());

        return result;
    }

    //描述性统计
    @RequestMapping(value = "/api/descriptiveStatistics")
    @ResponseBody
    public Map descriptiveStatistics(@NotNull @RequestBody List<InputData> dataList)
    {
        System.out.println(dataList);
        Map result=descriptivesService.descriptiveStatistics(dataList,getSavePath());
        return result;
    }

    //可靠性分析
    @RequestMapping(value = "/api/reliabilityAnalysis")
    @ResponseBody
    public Map reliabilityAnalysis(@NotNull @RequestBody RequestData requestData)
    {
        System.out.println(requestData.getAction());
        System.out.println(requestData.getDataList());
        Map result=descriptivesService.reliabilityAnalysis(requestData.getDataList());
        return result;
    }

    // T检验
    @RequestMapping(value = "/api/T-test")
    @ResponseBody
    public Map t_test(@NotNull @RequestBody RequestData requestData)
    {
        System.out.println(requestData.getAction());
        System.out.println(requestData.getDataList());
        Map result=null;
        //独立 t检验
        if("independentSample".equals(requestData.getAction()))
            result=t_testsService.independentSample(requestData.getDataList());
        //配对 t检验
        else if("pairedSample".equals(requestData.getAction()))
            result=t_testsService.pairedSample(requestData.getDataList());
        //单样本 t检验
        else if("oneSample".equals(requestData.getAction()))
            result=t_testsService.oneSample(requestData.getSingleData(),requestData.getDataList());
        //贝叶斯独立样本 t检验
        else if("bayesIndependentSample".equals(requestData.getAction()))
            result=t_testsService.independentBayesSample(requestData.getDataList(),requestData.getSingleStr());
        //贝叶斯配对 t检验
        else if("bayesPairedSample".equals(requestData.getAction()))
            result=t_testsService.bayesPairedSample(requestData.getDataList(),requestData.getSingleStr());
        //贝叶斯单样本 t检验
        else if("bayesOneSample".equals(requestData.getAction()))
            result=t_testsService.bayesOneSample(requestData.getDataList(),requestData.getSingleStr(),requestData.getSingleData());
        //
        return result;
    }

    // 方差分析
    @RequestMapping(value = "/api/ANOVA")
    @ResponseBody
    public Map anova(@NotNull @RequestBody RequestData requestData)
    {
        System.out.println(requestData.getAction());
        System.out.println(requestData.getDataList());

        Map result=null;
        if("ANOVA".equals(requestData.getAction()))
            result=anovaService.anova(requestData.getDataList());
        else if("ANCOVA".equals(requestData.getAction()))
            result=anovaService.ancova(requestData.getDataList());
        else if("MANOVA".equals(requestData.getAction()))
            result=anovaService.manova(requestData.getDataList());
        return result;
    }

    // 频率性分析
    @RequestMapping(value = "/api/Frequencies")
    @ResponseBody
    public Map frequencies(@NotNull @RequestBody RequestData requestData)
    {
        System.out.println(requestData.getAction());
        System.out.println(requestData.getDataList());

        Map result=null;
        //二项分布检验
        if("BinomialTest".equals(requestData.getAction()))
            result=frequenciesService.binomialTest(requestData.getDataList());
        //对数线性回归
        else if("Log-LinearRegression".equals(requestData.getAction()))
            result=frequenciesService.log_linearRegression(requestData.getDataList());
        //列联表
        else if("ContingencyTables".equals(requestData.getAction()))
            result=frequenciesService.contingencyTables(requestData.getDataList());
        //贝叶斯列联表
        else if("bayesContingencyTables".equals(requestData.getAction()))
        {
            String sampleType=String.valueOf(requestData.getMapData().get("sampleType"));//样本类型
            String fixedMargin=String.valueOf(requestData.getMapData().get("fixedMargin"));//当类型为 indepMulti时的fixedMargin类型
            result=frequenciesService.bayesContingencyTables(requestData.getDataList(),sampleType,fixedMargin);
        }

        return result;
    }

    //因素分析
    @RequestMapping(value = "/api/Factor")
    @ResponseBody
    public Map factor(@NotNull @RequestBody RequestData requestData)
    {
        System.out.println(requestData.getAction());
        System.out.println(requestData.getDataList());

        Map result=null;
        //主成分分析
        if("PrincipalComponentAnalysis".equals(requestData.getAction()))
            result=factorService.PrincipalComponentAnalysis(requestData.getDataList(),getSavePath());
        //探索性因子分析
        else if("ExploratoryFactorAnalysis".equals(requestData.getAction()))
            result=factorService.ExploratoryFactorAnalysis(requestData.getDataList(),requestData.getSingleData(),getSavePath());

        return result;
    }

    //获得存放图片结果的路径
    @Autowired
    HttpServletRequest request;
    public String getSavePath()
    {
        //设置文件夹路径
        String savePath_old=request.getSession().getServletContext().getRealPath("/")
                +"static/save_pics/";
        String savePath=savePath_old.replace("\\","/");
        //判断文件夹是否存在，不存在则创建
        File fileDir=new File(savePath);
        if(!fileDir.exists())
        {
            fileDir.mkdirs();
        }
        //真正 R可识别的路径
        String rPath=savePath.replace("/","//");
        return rPath;
    }
}
/*//线性回归
    @RequestMapping(value = "/api/linearRegression")
    @ResponseBody
    public Map linearRegression(@NotNull @RequestBody List<InputData> dataList)
    {
        System.out.println(dataList);
        Map result=regressionService.linearRegression(dataList);
        return result;
    }

    //逻辑回归
    @RequestMapping(value = "/api/logisticRegression")
    @ResponseBody
    public Map logisticRegression(@NotNull @RequestBody List<InputData> dataList)
    {
        System.out.println(dataList);
        Map result=regressionService.logisticRegression(dataList);
        return result;
    }

    //泊松回归
    @RequestMapping(value = "/api/poissonRegression")
    @ResponseBody
    public Map poissonRegression(@NotNull @RequestBody List<InputData> dataList)
    {
        System.out.println(dataList);
        Map result=regressionService.poissonRegression(dataList);
        return result;
    }

    //相关性
    @RequestMapping(value = "/api/correlation")
    @ResponseBody
    public Map correlation(@NotNull @RequestBody List<InputData> dataList)
    {
        System.out.println(dataList);
        Map result=regressionService.correlation(dataList,getSavePath());
        return result;
    }*/