package com.xqy.es.dao;

import com.xqy.es.entity.dto.KeyWord;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author xu7777777
 * @date 2020/6/15 10:00 AM
 */
public interface KeyWordDao {
    /**
     * 查询
     * @return   关键字列表
     */
    @Select("<script> " +
            "   select " +
            "       id, " +
            "       keyword " +
            "   from search_keyword s" +
            "</script> ")
    List<KeyWord> getAllKeyWords();
}
