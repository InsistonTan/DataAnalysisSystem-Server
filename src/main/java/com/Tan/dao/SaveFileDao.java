package com.Tan.dao;
import com.Tan.domain.SaveFile;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author TanJifeng
 * @Description 上传文件的持久层接口
 * @date 2020/6/14 19:41
 */
@Repository
public interface SaveFileDao {

    //通过uid和index获得某一个记录
    @Select("select * from savefiles where `index`=#{index} and uid=#{uid}")
    SaveFile getOneByIndexAndUid(SaveFile file);

    //选择此 uid的所有上传记录
    @Select("select * from savefiles where uid=#{uid} order by `index` desc")
    List<SaveFile> getAllByUid(@Param("uid") String uid);

    //增加上传文件记录
    @Insert("insert into savefiles(uid,filename,url,time,type) values(#{uid},#{filename},#{url},#{time},#{type})")
    boolean addSaveFile(SaveFile file);

    //删除记录
    @Delete("delete from savefiles where `index`=#{index} and uid=#{uid}")
    boolean deleteSaveFile(SaveFile file);

    //选择某个 index的公共文件
    @Select("select * from savefiles where `index`=#{index} and type='public'")
    SaveFile getOnePubicByIndex(SaveFile file);

    //选择所有公共文件
    @Select("select * from savefiles where type='public' order by `index` desc")
    List<SaveFile> getAllPublic();

}
