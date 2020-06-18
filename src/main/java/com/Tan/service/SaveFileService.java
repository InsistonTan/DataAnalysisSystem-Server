package com.Tan.service;
import com.Tan.dao.SaveFileDao;
import com.Tan.domain.SaveFile;
import org.jetbrains.annotations.NotNull;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TanJifeng
 * @Description
 * @date 2020/6/14 20:18
 */
@Service
public class SaveFileService {
    @Autowired
    private SaveFileDao saveFileDao;

    //调用 R读取文件数据
    public Map readFileData(String filePath, String type)
    {
        RConnection rc=null;//Rserve连接
        Map result=new HashMap();//返回前端的结果

        //用 R进行读取文件数据------------------------------------------
        try{
            rc=new RConnection();
            //判断类型
            if("excel".equalsIgnoreCase(type))
                rc.voidEval("data <- xlsx::read.xlsx2(file='"+filePath+"',sheetIndex=1)");
            else if("csv".equalsIgnoreCase(type))
                rc.voidEval("data <- read.csv(file='"+filePath+"',header=T,na.strings=c('NA'))");
            else if("sav".equalsIgnoreCase(type))
                rc.voidEval("data <- foreign::read.spss('"+filePath+"')");
            //得到数据
            REXP rexp=rc.eval("data");
            RList dataList=rexp.asList();//数据列表
            String[] colNames=null;//列名
            //获得列名
            if("excel".equalsIgnoreCase(type)||"csv".equalsIgnoreCase(type))
                colNames=rexp._attr().asList().at(0).asStrings();
            else if("sav".equalsIgnoreCase(type))
                colNames=rexp._attr().asList().at(1).asStrings();
            //判断列名为空
            if(colNames==null)
            {
                result.put("statu","failed");
                result.put("msg","后台处理数据错误!\nErrorMsg:数据文件没有数据或没有列名");
                return result;
            }
            //存放每一列数据
            Map readData=new HashMap();
            //循环处理每一列数据
            for(int i=0;i<colNames.length;i++)
            {
                //获取第i列数据
                String[] value=dataList.at(i).asStrings();
                //将第i列数据存进readData
                readData.put(colNames[i],value);
            }
            result.put("data",readData);
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

    //通过index获得某一个记录
    public SaveFile getOneByIndexAndUid(SaveFile file)
    {
        return saveFileDao.getOneByIndexAndUid(file);
    }

    //选择此 uid的所有上传记录
    public List<SaveFile> getAllByUid(String uid)
    {
        return saveFileDao.getAllByUid(uid);
    }

    //增加上传文件记录
    public boolean addSaveFile(@NotNull SaveFile file)
    {
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String time=dateFormat.format(new Date());
        file.setTime(time);
        return saveFileDao.addSaveFile(file);
    }

    //删除记录
    public boolean deleteSaveFile(SaveFile file)
    {
        return saveFileDao.addSaveFile(file);
    }

}
