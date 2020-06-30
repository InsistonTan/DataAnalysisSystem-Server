package com.Tan.controller;
import com.Tan.domain.SaveFile;
import com.Tan.service.SaveFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author TanJifeng
 * @Description 用户上传文件控制器
 * @date 2020/6/14 20:23
 */
@Controller
public class SaveFileController {
    @Autowired
    private SaveFileService saveFileService;
    @Autowired
    private HttpServletRequest request;
    private final String[] FileTypes={"xlsx","xls","sav","csv"};//支持的文件类型

    //用户上传数据文件（非持久保存）
    @RequestMapping(value = "/api/uploadFile")
    @ResponseBody
    public Map upload(MultipartFile file,String type)
    {
        Map result=null;
        //处理上传的文件---------------------------------------------------------------------
        if(file==null)//文件为空
        {
            result=new HashMap();
            result.put("statu","error");
            result.put("msg","No file selected！");
            return result;
        }
        //文件名
        String file_name=file.getOriginalFilename();
        System.out.println("上传文件名:"+file_name+"大小:"+file.getSize());
        //获取文件后缀并判断文件类型是否正确
        String[] temp=file_name.split("\\.");
        String suffix=temp[temp.length-1];
        System.out.println("后缀是:"+suffix);
        if(suffix==null||suffix.equals(file_name))
        {
            result=new HashMap();
            result.put("statu","error");
            result.put("msg","The file has no suffix！");
            return result;
        }

        //设置上传文件夹路径
        String savePath_foot="static/upload_files/";
        String savePath_old=request.getSession().getServletContext().getRealPath("/")
                +savePath_foot;
        //将路径中的'\'换成'/'（为了适配windows和linux）
        String savePath=savePath_old.replace("\\","/");
        System.out.println("保存的路径是:"+savePath);
        //判断文件夹是否存在，不存在则创建
        File fileDir=new File(savePath);
        if(!fileDir.exists())
        {
            fileDir.mkdirs();
        }

        //保存文件
        //String fileName=String.valueOf(new Date().getTime());//没有后缀的文件名
        String fullFileName="tempFile."+suffix;//完整的文件名(有后缀)
        try {
            file.transferTo(new File(savePath+fullFileName));
        } catch (IOException e) {e.printStackTrace();}

        //将文件路径处理成 R 可识别的路径(例如 \\root\\data\\data.xlsx)
        String file_path=savePath+fullFileName;
        String file_url=file_path.replace("/","//");

        //处理文件
        result=saveFileService.readFileData(file_url,type);
        //返回结果
        return result;
    }

    //用户上传数据文件（持久保存）
    @RequestMapping(value = "/api/saveFile")
    @ResponseBody
    public Map saveFile(MultipartFile file)
    {
        Map result=result=new HashMap();
        //检查是否登录
        Object uid_obj=request.getSession().getAttribute("uid");
        if(uid_obj==null)
        {
            result.put("statu","error");
            result.put("msg","You have not signed in！");
            return result;
        }
        String uid=uid_obj.toString();
        //处理上传的文件---------------------------------------------------------------------
        if(file==null)//文件为空
        {
            result.put("statu","error");
            result.put("msg","No file selected！");
            return result;
        }
        //文件名
        String file_name=file.getOriginalFilename();
        System.out.println("上传文件名:"+file_name+"大小:"+file.getSize());
        //获取文件后缀并判断文件类型是否正确
        String[] temp=file_name.split("\\.");
        String suffix=temp[temp.length-1];
        System.out.println("后缀是:"+suffix);
        //获取没有后缀的文件名
        StringBuilder name_noSuffix=new StringBuilder();
        if(temp.length>1)
        {
            for(int i=0;i<temp.length-1;i++)
            {
                if(i==temp.length-2)
                    name_noSuffix.append(temp[i]);
                else
                    name_noSuffix.append(temp[i]+".");
            }
        }
        else name_noSuffix.append(file_name);
        if(suffix==null||suffix.equals(file_name))
        {
            result.put("statu","error");
            result.put("msg","The file has no suffix！");
            return result;
        }
        //检查文件类型是否支持
        for(int i=0;i<FileTypes.length;i++)
        {
            if(FileTypes[i].equalsIgnoreCase(suffix))//该文件是支持的类型
                break;
            if(i==(FileTypes.length-1))//遍历所有的文件类型都没有匹配，则不支持
            {
                result.put("statu","error");
                result.put("msg","Unsupported file type:"+suffix);
                return result;
            }
        }

        //设置上传文件夹路径
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String date=dateFormat.format(new Date());
        String savePath_foot="static/upload_files/"+date+"/";
        String savePath_old=request.getSession().getServletContext().getRealPath("/")
                +savePath_foot;
        //将路径中的'\'换成'/'（为了适配windows和linux）
        String savePath=savePath_old.replace("\\","/");
        System.out.println("保存的路径是:"+savePath);
        //判断文件夹是否存在，不存在则创建
        File fileDir=new File(savePath);
        if(!fileDir.exists())
        {
            fileDir.mkdirs();
        }

        //保存文件
        String fileName=name_noSuffix+"-"+new Date().getTime();//没有后缀的文件名
        String fullFileName=fileName+"."+suffix;//完整的文件名(有后缀)
        try {
            file.transferTo(new File(savePath+fullFileName));
        } catch (IOException e) {e.printStackTrace();}

        //将文件路径处理成 R 可识别的路径(例如 \\root\\data\\data.xlsx)
        String file_path=savePath+fullFileName;
        String url=file_path.replace("/","//");
        String filename=file_name;
        SaveFile saveFile=new SaveFile();
        saveFile.setUid(uid);
        saveFile.setFilename(filename);
        saveFile.setUrl(url);
        boolean res=saveFileService.addSaveFile(saveFile);
        if(res==true)
        {
            result.put("statu","success");
        }
        else
        {
            result.put("statu","failed");
            result.put("msg","File save failed");
        }
        //System.out.println(result);
        //返回结果
        return result;
    }

    //获取登陆用户的所有上传文件记录
    @RequestMapping(value = "/api/getMyFiles")
    @ResponseBody
    public Map getMyFiles()
    {
        //
        //String name=(String) request.getSession().getAttribute("username");
        //String id=(String) request.getSession().getAttribute("uid");
        //System.out.println("getMyFiles:"+name+"-"+id);
        //
        Map result=new HashMap();
        Object obj_uid=request.getSession().getAttribute("uid");
        if(obj_uid!=null)
        {
            String uid=String.valueOf(obj_uid);
            result.put("statu","success");
            result.put("data",saveFileService.getAllByUid(uid));
        }
        else
        {
            result.put("statu","failed");
            result.put("msg","You have not signed in.");
        }
        return result;
    }

    //打开某个已上传的文件
    @RequestMapping(value = "/api/openMyFile")
    @ResponseBody
    public Map openMyFile(@RequestBody SaveFile file)
    {
        Map result=new HashMap();
        Object obj_uid=request.getSession().getAttribute("uid");
        if(obj_uid==null)//用户未登陆
        {
            result.put("statu","failed");
            result.put("msg","You have not signed in.");
            return result;
        }
        //已登陆
        String uid=String.valueOf(obj_uid);
        file.setUid(uid);
        SaveFile tempFile=saveFileService.getOneByIndexAndUid(file);
        if(tempFile==null)//文件记录不存在
        {
            result.put("statu","failed");
            result.put("msg","You did not upload this file.");
            return result;
        }
        //处理文件---------------------------------------------------------
        String type;//要处理的类型，如 excel、sav、csv
        //通过文件后缀判断类型
        String[] strs=tempFile.getFilename().split("\\.");
        String suffix=strs[strs.length-1];
        if("xlsx".equalsIgnoreCase(suffix)||"xls".equalsIgnoreCase(suffix))
            type="excel";
        else if("csv".equalsIgnoreCase(suffix))
            type="csv";
        else if("sav".equalsIgnoreCase(suffix))
            type="sav";
        else
        {
            result.put("statu","failed");
            result.put("msg","The file type is not supported.");
            return result;
        }
        //处理文件数据
        result=saveFileService.readFileData(tempFile.getUrl(),type);
        //返回结果
        return result;
    }

    //删除某个文件记录
    @RequestMapping(value = "/api/deleteMyFile")
    @ResponseBody
    public Map deleteMyFile(@RequestBody SaveFile file)
    {
        Map result=new HashMap();
        Object uid_obj=request.getSession().getAttribute("uid");
        if(uid_obj==null)
        {
            result.put("statu","failed");
            result.put("msg","You have not signed in.");
            return result;
        }
        String uid=String.valueOf(uid_obj);
        file.setUid(uid);
        boolean res=saveFileService.deleteSaveFile(file);
        if(res)
        {
            result.put("statu","success");
            return result;
        }
        else
        {
            result.put("statu","failed");
            result.put("msg","Failed to delete.");
            return result;
        }
    }

    //管理员上传公共文件（持久保存）
    @RequestMapping(value = "/api/savePublicFile")
    @ResponseBody
    public Map savePublicFile(MultipartFile file)
    {
        Map result=result=new HashMap();
        //检查是否是管理员登录
        Object uid_obj=request.getSession().getAttribute("uid");
        if(uid_obj==null)
        {
            result.put("statu","error");
            result.put("msg","You have not signed in！");
            return result;
        }
        String uid=uid_obj.toString();
        String username=(String)request.getSession().getAttribute("username");
        if(!username.equals("admin"))//不是管理员
        {
            result.put("statu","error");
            result.put("msg","You do not have permission！");
            return result;
        }
        //处理上传的文件---------------------------------------------------------------------
        if(file==null)//文件为空
        {
            result.put("statu","error");
            result.put("msg","No file selected！");
            return result;
        }
        //文件名
        String file_name=file.getOriginalFilename();
        System.out.println("管理员上传文件名:"+file_name+"大小:"+file.getSize());
        //获取文件后缀
        String[] temp=file_name.split("\\.");
        String suffix=temp[temp.length-1];//后缀
        //获取没有后缀的文件名
        StringBuilder name_noSuffix=new StringBuilder();
        if(temp.length>1)
        {
            for(int i=0;i<temp.length-1;i++)
            {
                if(i==temp.length-2)
                    name_noSuffix.append(temp[i]);
                else
                    name_noSuffix.append(temp[i]+".");
            }
        }
        else name_noSuffix.append(file_name);

        //设置上传文件夹路径
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String date=dateFormat.format(new Date());
        String savePath_foot="static/upload_files/"+date+"/";
        String savePath_old=request.getSession().getServletContext().getRealPath("/")
                +savePath_foot;
        //将路径中的'\'换成'/'（为了适配windows和linux）
        String savePath=savePath_old.replace("\\","/");
        System.out.println("保存的路径是:"+savePath);
        //判断文件夹是否存在，不存在则创建
        File fileDir=new File(savePath);
        if(!fileDir.exists())
        {
            fileDir.mkdirs();
        }

        //保存文件
        String fileName=name_noSuffix+"-"+new Date().getTime();//没有后缀的文件名
        String fullFileName=fileName+"."+suffix;//完整的文件名(有后缀)
        try {
            file.transferTo(new File(savePath+fullFileName));
        } catch (IOException e) {e.printStackTrace();}

        //将文件路径处理成 R 可识别的路径(例如 \\root\\data\\data.xlsx)
        String file_path=savePath+fullFileName;
        String url=file_path.replace("/","//");
        //保存记录到数据库
        String filename=file_name;
        SaveFile saveFile=new SaveFile();
        saveFile.setUid(uid);
        saveFile.setFilename(filename);
        saveFile.setUrl(url);
        saveFile.setType("public");//公共文件
        boolean res=saveFileService.addSaveFile(saveFile);
        if(res==true)
        {
            result.put("statu","success");
        }
        else
        {
            result.put("statu","failed");
            result.put("msg","File save failed");
        }
        //System.out.println(result);
        //返回结果
        return result;
    }

    //获取所有的公共文件
    @RequestMapping(value = "/api/getPublicFiles")
    @ResponseBody
    public Map getPublicFiles()
    {
        Map result=new HashMap();
        List<SaveFile> res=saveFileService.getAllPublic();
        result.put("statu","success");
        result.put("data",res);
        return result;
    }

    //打开某个公共文件
    @RequestMapping(value = "/api/openPublicFile")
    @ResponseBody
    public Map openPublicFile(@RequestBody SaveFile file)
    {
        Map result=new HashMap();
        //
        SaveFile tempFile=saveFileService.getOnePublicByIndex(file);
        if(tempFile==null)//文件记录不存在
        {
            result.put("statu","failed");
            result.put("msg","Public files do not have this file.");
            return result;
        }
        //处理文件---------------------------------------------------------
        String type;//要处理的类型，如 excel、sav、csv
        //通过文件后缀判断类型
        String[] strs=tempFile.getFilename().split("\\.");
        String suffix=strs[strs.length-1];
        if("xlsx".equalsIgnoreCase(suffix)||"xls".equalsIgnoreCase(suffix))
            type="excel";
        else if("csv".equalsIgnoreCase(suffix))
            type="csv";
        else if("sav".equalsIgnoreCase(suffix))
            type="sav";
        else
        {
            result.put("statu","failed");
            result.put("msg","The file type is not supported.");
            return result;
        }
        //处理文件数据
        result=saveFileService.readFileData(tempFile.getUrl(),type);
        //返回结果
        return result;
    }

    //打开某个demo文件
    @RequestMapping(value = "/api/openDemoFile")
    @ResponseBody
    public Map openDemoFile(@RequestBody SaveFile file)
    {
        Map result=new HashMap();
        //文件记录不存在
        if(file.getUrl()==null)
        {
            result.put("statu","failed");
            result.put("msg","The information of this file error.");
            return result;
        }
        //处理文件---------------------------------------------------------
        String type;//要处理的类型，如 excel、sav、csv
        //通过文件后缀判断类型
        String[] strs=file.getUrl().split("\\.");
        String suffix=strs[strs.length-1];
        if("xlsx".equalsIgnoreCase(suffix)||"xls".equalsIgnoreCase(suffix))
            type="excel";
        else if("csv".equalsIgnoreCase(suffix))
            type="csv";
        else if("sav".equalsIgnoreCase(suffix))
            type="sav";
        else
        {
            result.put("statu","failed");
            result.put("msg","The file type is not supported.");
            return result;
        }
        //处理文件url为 R可识别的路径
        String filePath_old=request.getSession().getServletContext().getRealPath("/")
                +file.getUrl();
        //将路径中的'\'换成'/'（为了适配windows和linux）
        String filePath=filePath_old.replace("\\","/");
        String R_filePath=filePath.replace("/","//");
        //处理文件数据
        result=saveFileService.readFileData(R_filePath,type);
        //返回结果
        return result;
    }
}
