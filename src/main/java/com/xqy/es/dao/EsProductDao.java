package com.xqy.es.dao;

import com.xqy.es.entity.dto.EsProduct;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 搜索系统中的商品管理自定义Dao
 * Created by macro on 2018/6/19.
 */
public interface EsProductDao {
    /**
     * 查询
     * @param id ID
     * @return   商品列表
     */
    @Select("<script> " +
            "   select " +
            "       id, " +
            "       title, " +
            "       img, " +
            "       price, " +
            "       cur_price curPrice, " +
            "       sales_volume salesVolume, " +
            "       intro " +
            "   from goods g" +
            "       <where> "+
            "           <if test=\"id != null or id == ''\"> " +
            "               and g.id = #{id} " +
            "           </if> " +
            "       </where> "+
            "</script> ")
    List<EsProduct> getAllEsProductList(@Param("id") Long id);
}
