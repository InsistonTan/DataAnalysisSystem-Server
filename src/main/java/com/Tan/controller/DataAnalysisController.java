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

    //线性回归
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
        if("independentSample".equals(requestData.getAction()))
            result=t_testsService.independentSample(requestData.getDataList());
        else if("pairedSample".equals(requestData.getAction()))
            result=t_testsService.pairedSample(requestData.getDataList());
        else if("oneSample".equals(requestData.getAction()))
            result=t_testsService.oneSample(requestData.getSingleData(),requestData.getDataList());

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
        if("BinomialTest".equals(requestData.getAction()))
            result=frequenciesService.binomialTest(requestData.getDataList());
        else if("Log-LinearRegression".equals(requestData.getAction()))
            result=frequenciesService.log_linearRegression(requestData.getDataList());
        else if("ContingencyTables".equals(requestData.getAction()))
            result=frequenciesService.contingencyTables(requestData.getDataList());

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
        if("PrincipalComponentAnalysis".equals(requestData.getAction()))
            result=factorService.PrincipalComponentAnalysis(requestData.getDataList(),getSavePath());
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
