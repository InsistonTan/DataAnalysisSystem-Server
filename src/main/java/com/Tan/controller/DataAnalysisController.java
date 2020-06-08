package com.Tan.controller;
import com.Tan.domain.InputData;
import com.Tan.domain.RequestData;
import com.Tan.service.DataAnalysisService;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DataAnalysisController {
    @Autowired
    DataAnalysisService service;

    //线性回归
    @RequestMapping(value = "/linearRegression")
    @ResponseBody
    public Map linearRegression(@RequestBody List<InputData> dataList)
    {
        System.out.println(dataList);
        Map result=service.linearRegression(dataList);
        return result;
    }

    //逻辑回归
    @RequestMapping(value = "/logisticRegression")
    @ResponseBody
    public Map logisticRegression(@RequestBody List<InputData> dataList)
    {
        System.out.println(dataList);
        Map result=service.logisticRegression(dataList);
        return result;
    }

    //相关性
    @RequestMapping(value = "/correlation")
    @ResponseBody
    public Map correlation(@RequestBody List<InputData> dataList)
    {
        System.out.println(dataList);
        Map result=service.correlation(dataList);
        return result;
    }

    //相关性
    @RequestMapping(value = "/descriptiveStatistics")
    @ResponseBody
    public Map descriptiveStatistics(@RequestBody List<InputData> dataList)
    {
        System.out.println(dataList);
        Map result=service.descriptiveStatistics(dataList);
        return result;
    }

    //相关性
    @RequestMapping(value = "/reliabilityAnalysis")
    @ResponseBody
    public Map reliabilityAnalysis(@RequestBody RequestData requestData)
    {
        System.out.println(requestData.getAction());
        System.out.println(requestData.getDataList());
        Map result=service.reliabilityAnalysis(requestData.getDataList());
        return result;
    }

    // T检验
    @RequestMapping(value = "/T-test")
    @ResponseBody
    public Map t_test(@RequestBody RequestData requestData)
    {
        System.out.println(requestData.getAction());
        System.out.println(requestData.getDataList());
        Map result=null;
        if("independentSample".equals(requestData.getAction()))
            result=service.independentSample(requestData.getDataList());
        else if("pairedSample".equals(requestData.getAction()))
            result=service.pairedSample(requestData.getDataList());
        else if("oneSample".equals(requestData.getAction()))
            result=service.oneSample(requestData.getSingleData(),requestData.getDataList());

        return result;
    }

    // 方差分析
    @RequestMapping(value = "/ANOVA")
    @ResponseBody
    public Map anova(@RequestBody RequestData requestData)
    {
        System.out.println(requestData.getAction());
        System.out.println(requestData.getDataList());
        Map result=null;

        if("ANOVA".equals(requestData.getAction()))
            result=service.anova(requestData.getDataList());
        else if("ANCOVA".equals(requestData.getAction()))
            result=service.ancova(requestData.getDataList());
        else if("MANOVA".equals(requestData.getAction()))
            result=service.manova(requestData.getDataList());
        return result;
    }
}
