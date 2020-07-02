package com.xqy.es.service.impl;

import com.xqy.es.dao.EsProductDao;
import com.xqy.es.dao.KeyWordDao;
import com.xqy.es.entity.consts.RedisConst;
import com.xqy.es.entity.dto.EsProduct;
import com.xqy.es.entity.dto.EsProductRelatedInfo;
import com.xqy.es.entity.dto.KeyWord;
import com.xqy.es.repository.EsProductRepository;
import com.xqy.es.service.EsProductService;
import com.xqy.es.util.RedisUtil;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;


/**
 * 商品搜索管理Service实现类
 * Created by macro on 2018/6/19.
 */
@Service
public class EsProductServiceImpl implements EsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductServiceImpl.class);
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private KeyWordDao keyWordDao;
    @Resource
    private EsProductDao productDao;
    @Resource
    private EsProductRepository productRepository;
    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 从数据库中返回所有商品信息
     *
     * @return 商品总数
     */
    @Override
    public int importAll() {
        List<EsProduct> esProductList = productDao.getAllEsProductList(null);
        // 将商品信息保存到es
        Iterable<EsProduct> esProductIterable = productRepository.saveAll(esProductList);
        Iterator<EsProduct> iterator = esProductIterable.iterator();
        int result = 0;
        // 统计商品个数
        while (iterator.hasNext()) {
            result++;
            iterator.next();
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public EsProduct create(Long id) {
        EsProduct result = null;
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
        if (esProductList.size() > 0) {
            EsProduct esProduct = esProductList.get(0);
            result = productRepository.save(esProduct);
        }
        return result;
    }

    @Override
    public void delete(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            List<EsProduct> esProductList = new ArrayList<>();
            for (Long id : ids) {
                EsProduct esProduct = new EsProduct();
                esProduct.setId(id);
                esProductList.add(esProduct);
            }
            productRepository.deleteAll(esProductList);
        }
    }

    @Override
    public Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize) {
        // 应当对传入参数进行安全性检查
        String obj = redisUtil.get(RedisConst.SEARCH_RECORDS_KEY);
        if (!StringUtils.isEmpty(keyword)) {
            if (!StringUtils.isEmpty(obj) && !"".equals(obj) && !"null".equals(obj)) {
                List<String> result = new ArrayList<>(Arrays.asList(obj.split(";;;")));
                if (!result.contains(keyword)) {
                    result.add(keyword);
                }
                redisUtil.set(RedisConst.SEARCH_RECORDS_KEY, String.join(";;;", result));
            } else {
                redisUtil.set(RedisConst.SEARCH_RECORDS_KEY, keyword);
            }
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return productRepository.findByTitle(keyword, pageable);
    }

    @Override
    public Page<EsProduct> search(String keyword, Integer sort, Integer pageNum, Integer pageSize) {
        // about redis for search records
        String obj = redisUtil.get(RedisConst.SEARCH_RECORDS_KEY);
        if (!StringUtils.isEmpty(keyword)) {
            if (!StringUtils.isEmpty(obj) && !"".equals(obj) && !"null".equals(obj)) {
                List<String> result = new ArrayList<>(Arrays.asList(obj.split(";;;")));
                if (!result.contains(keyword)) {
                    result.add(keyword);
                }
                redisUtil.set(RedisConst.SEARCH_RECORDS_KEY, String.join(";;;", result));
            } else {
                redisUtil.set(RedisConst.SEARCH_RECORDS_KEY, keyword);
            }
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //分页
        nativeSearchQueryBuilder.withPageable(pageable);
        //过滤
//        if (brandId != null || productCategoryId != null) {
//            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//            if (brandId != null) {
//                boolQueryBuilder.must(QueryBuilders.termQuery("brandId", brandId));
//            }
//            if (productCategoryId != null) {
//                boolQueryBuilder.must(QueryBuilders.termQuery("productCategoryId", productCategoryId));
//            }
//            nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
//        }
        //搜索
//        if (StringUtils.isEmpty(keyword)) {
//            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());
//        } else {
//            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
//            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("name", keyword),
//                    ScoreFunctionBuilders.weightFactorFunction(10)));
//            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("subTitle", keyword),
//                    ScoreFunctionBuilders.weightFactorFunction(5)));
//            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("keywords", keyword),
//                    ScoreFunctionBuilders.weightFactorFunction(2)));
//            FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
//            filterFunctionBuilders.toArray(builders);
//            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(builders);
////                    .scoreMode(FunctionScoreQuery.SUM)
////                    .setMinScore(2);
//            nativeSearchQueryBuilder.withQuery(functionScoreQueryBuilder);
//        }
        //排序
        if (sort == 1) {
            //按新品从新到旧
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC));
        } else if (sort == 2) {
            //按销量从高到低
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("curPrice").order(SortOrder.DESC));
        } else if (sort == 3) {
            //按价格从低到高
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("salesVolume").order(SortOrder.DESC));
        } else if (sort == 4) {
            //按价格从高到低
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("curPrice").order(SortOrder.ASC));
        } else {
            //按相关度
            nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
        }

        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("title", keyword).minimumShouldMatch("75%"));
        nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.ASC));
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
        return productRepository.search(searchQuery);
    }

//    @Override
//    public Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize) {
//        Pageable pageable = PageRequest.of(pageNum, pageSize);
//        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
//        if (esProductList.size() > 0) {
//            EsProduct esProduct = esProductList.get(0);
//            String keyword = esProduct.getName();
//            Long brandId = esProduct.getBrandId();
//            Long productCategoryId = esProduct.getProductCategoryId();
//            //根据商品标题、品牌、分类进行搜索
//            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
//            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("name", keyword),
//                    ScoreFunctionBuilders.weightFactorFunction(8)));
//            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("subTitle", keyword),
//                    ScoreFunctionBuilders.weightFactorFunction(2)));
//            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("keywords", keyword),
//                    ScoreFunctionBuilders.weightFactorFunction(2)));
//            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("brandId", brandId),
//                    ScoreFunctionBuilders.weightFactorFunction(10)));
//            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("productCategoryId", productCategoryId),
//                    ScoreFunctionBuilders.weightFactorFunction(6)));
//            FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
//            filterFunctionBuilders.toArray(builders);
//            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(builders)
//                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
//                    .setMinScore(2);
//            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
//            builder.withQuery(functionScoreQueryBuilder);
//            builder.withPageable(pageable);
//            NativeSearchQuery searchQuery = builder.build();
//            LOGGER.info("DSL:{}", searchQuery.getQuery().toString());
//            return productRepository.search(searchQuery);
//        }
//        return new PageImpl<>(null);
//    }

    @Override
    public EsProductRelatedInfo searchRelatedInfo(String keyword) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //搜索条件
        if (StringUtils.isEmpty(keyword)) {
            builder.withQuery(QueryBuilders.matchAllQuery());
        } else {
            builder.withQuery(QueryBuilders.multiMatchQuery(keyword, "name", "subTitle", "keywords"));
        }
        //聚合搜索品牌名称
        builder.addAggregation(AggregationBuilders.terms("brandNames").field("brandName"));
        //集合搜索分类名称
        builder.addAggregation(AggregationBuilders.terms("productCategoryNames").field("productCategoryName"));
        //聚合搜索商品属性，去除type=1的属性
        AbstractAggregationBuilder aggregationBuilder = AggregationBuilders.nested("allAttrValues", "attrValueList")
                .subAggregation(AggregationBuilders.filter("productAttrs", QueryBuilders.termQuery("attrValueList.type", 1))
                        .subAggregation(AggregationBuilders.terms("attrIds")
                                .field("attrValueList.productAttributeId")
                                .subAggregation(AggregationBuilders.terms("attrValues")
                                        .field("attrValueList.value"))
                                .subAggregation(AggregationBuilders.terms("attrNames")
                                        .field("attrValueList.name"))));
        builder.addAggregation(aggregationBuilder);
        NativeSearchQuery searchQuery = builder.build();
        return elasticsearchTemplate.query(searchQuery, response -> {
            LOGGER.info("DSL:{}", searchQuery.getQuery().toString());
            return convertProductRelatedInfo(response);
        });
    }

    @Override
    public EsProduct detail(Long id) {
        Optional<EsProduct> optionalEsProduct = productRepository.findById(id);
        return optionalEsProduct.orElse(null);
    }

    @Override
    public List<KeyWord> keywords() {
        return keyWordDao.getAllKeyWords();
    }

    @Override
    public List<String> records() {
        String objStr = redisUtil.get(RedisConst.SEARCH_RECORDS_KEY);
        if (!StringUtils.isEmpty(objStr) && !"".equals(objStr) && !"null".equals(objStr)) {
            List<String> records = new ArrayList<>();
            List<String> tmp = Arrays.asList(objStr.split(";;;"));
            for (String aTmp : tmp) {
                if (!records.contains(aTmp)) {
                    records.add(aTmp);
                }
            }
            if (records.size() > 20) {
                records = records.subList(0, 20);
                redisUtil.set(RedisConst.SEARCH_RECORDS_KEY, String.join(";;;", records));
            }
            return records;
        }
        return null;
    }

    @Override
    public List<String> complete(String prefix) {

        //指定在哪个字段搜索
        String suggestField = "title.suggest";
        //获得最大suggest条数
        final int suggestMaxCount = 12;

        CompletionSuggestionBuilder suggestionBuilderDistrict = new CompletionSuggestionBuilder(suggestField).prefix(prefix).size(suggestMaxCount);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        //添加suggest
        suggestBuilder.addSuggestion("mall_suggest", suggestionBuilderDistrict);

        //设置查询builder的index,type,以及建议
        SearchRequestBuilder requestBuilder = this.elasticsearchTemplate.getClient().prepareSearch("mall-goods").setTypes("_doc").suggest(suggestBuilder);
        System.out.println(requestBuilder.toString());

        SearchResponse response = requestBuilder.get();
        //suggest实体
        Suggest suggest = response.getSuggest();

        //list
        ArrayList<String> suggests = new ArrayList<>();
        if (suggest != null) {
            //获取suggest,name任意string
            Suggest.Suggestion result = suggest.getSuggestion("mall_suggest");
            for (Object term : result.getEntries()) {

                if (term instanceof CompletionSuggestion.Entry) {
                    CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;
                    if (!item.getOptions().isEmpty()) {
                        //若item的option不为空,循环遍历
                        for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                            String tip = option.getText().toString();
                            if (suggest.size() > suggestMaxCount) {
                                break;
                            }
                            if (!suggests.contains(tip)) {
                                suggests.add(tip);
                            }
                        }
                    }
                }
            }
        }
        suggests.forEach(System.out::println);

        return suggests;
    }

    /**
     * 将返回结果转换为对象
     */
    private EsProductRelatedInfo convertProductRelatedInfo(SearchResponse response) {
        EsProductRelatedInfo productRelatedInfo = new EsProductRelatedInfo();
        Map<String, Aggregation> aggregationMap = response.getAggregations().getAsMap();
        //设置品牌
        Aggregation brandNames = aggregationMap.get("brandNames");
        List<String> brandNameList = new ArrayList<>();
        for (int i = 0; i < ((Terms) brandNames).getBuckets().size(); i++) {
            brandNameList.add(((Terms) brandNames).getBuckets().get(i).getKeyAsString());
        }
        productRelatedInfo.setBrandNames(brandNameList);
        //设置分类
        Aggregation productCategoryNames = aggregationMap.get("productCategoryNames");
        List<String> productCategoryNameList = new ArrayList<>();
        for (int i = 0; i < ((Terms) productCategoryNames).getBuckets().size(); i++) {
            productCategoryNameList.add(((Terms) productCategoryNames).getBuckets().get(i).getKeyAsString());
        }
        productRelatedInfo.setProductCategoryNames(productCategoryNameList);
        //设置参数
        Aggregation productAttrs = aggregationMap.get("allAttrValues");
        List<LongTerms.Bucket> attrIds = ((LongTerms) ((InternalFilter) ((InternalNested) productAttrs).getProperty("productAttrs")).getProperty("attrIds")).getBuckets();
        List<EsProductRelatedInfo.ProductAttr> attrList = new ArrayList<>();
        for (Terms.Bucket attrId : attrIds) {
            EsProductRelatedInfo.ProductAttr attr = new EsProductRelatedInfo.ProductAttr();
            attr.setAttrId((Long) attrId.getKey());
            List<String> attrValueList = new ArrayList<>();
            List<StringTerms.Bucket> attrValues = ((StringTerms) attrId.getAggregations().get("attrValues")).getBuckets();
            List<StringTerms.Bucket> attrNames = ((StringTerms) attrId.getAggregations().get("attrNames")).getBuckets();
            for (Terms.Bucket attrValue : attrValues) {
                attrValueList.add(attrValue.getKeyAsString());
            }
            attr.setAttrValues(attrValueList);
            if (!CollectionUtils.isEmpty(attrNames)) {
                String attrName = attrNames.get(0).getKeyAsString();
                attr.setAttrName(attrName);
            }
            attrList.add(attr);
        }
        productRelatedInfo.setProductAttrs(attrList);
        return productRelatedInfo;
    }
}
