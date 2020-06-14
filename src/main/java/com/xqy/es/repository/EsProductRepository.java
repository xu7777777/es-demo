package com.xqy.es.repository;

import com.xqy.es.entity.dto.EsProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 商品ES操作类
 * Created by macro on 2018/6/19.
 */
public interface EsProductRepository extends ElasticsearchRepository<EsProduct, Long> {
    /**
     * 搜索查询
     *
     * @param keyword           商品标题
     * @param page              分页信息
     * @return
     */
    Page<EsProduct> findByTitle(String keyword, Pageable page);

}
