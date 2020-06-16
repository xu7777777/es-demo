package com.xqy.es.controller;

import com.xqy.es.entity.dto.EsProduct;
import com.xqy.es.entity.dto.KeyWord;
import com.xqy.es.entity.vo.CommonPage;
import com.xqy.es.entity.vo.CommonResult;
import com.xqy.es.service.EsProductService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xu7777777
 * @date 2020/1/1
 */
@RestController
@RequestMapping("search")
public class SearchController {
    @Resource
    private EsProductService esProductService;

    /**
     * 简单查询
     * @param name      商品名称
     * @param pageNum   页码：def -> 0
     * @param pageSize  页面项数：def -> 5
     * @return
     */
    @RequestMapping(value = "/search/simple", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<EsProduct>> search(@RequestParam(required = false) String name,
                                                      @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                                      @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        Page<EsProduct> esProductPage = esProductService.search(name, pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(esProductPage));
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<EsProduct>> search(@RequestParam(required = false) String name,
                                                      @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                                      @RequestParam(required = false, defaultValue = "1000") Integer pageSize,
                                                      @RequestParam(required = false, defaultValue = "0") Integer sort) {
        Page<EsProduct> esProductPage = esProductService.search(name, sort, pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(esProductPage));
    }

    /**
     * 数据库商品信息导入到ES
     * @return result -> (code、message、data)
     */
    @RequestMapping(value = "/importAll", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<Integer> importAllList() {
        int count = esProductService.importAll();
        return CommonResult.success(count);
    }

    /**
     * 根据ID获取指定的商品
     * @return result -> (code、message、data)
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<EsProduct> detail(@RequestParam("id") Long id) {
        EsProduct esProduct = esProductService.detail(id);
        return CommonResult.success(esProduct);
    }

    /**
     * 搜索时关键字
     * @return List -> String
     */
    @RequestMapping(value = "/keywords", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<KeyWord>> keywords() {
        List<KeyWord> keywords = esProductService.keywords();
        return CommonResult.success(keywords);
    }

    /**
     * 获取所有的搜索记录（暂不区分用户）
     */
    @RequestMapping(value = "/records", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<String>> records() {
        List<String> records = esProductService.records();
        return CommonResult.success(records);
    }


    /**
     * 补全用户搜索
     */
    @RequestMapping(value = "/complete", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<String>> complete(@RequestParam("prefix") String prefix) {
        List<String> completes = esProductService.complete(prefix);
        return CommonResult.success(completes);
    }

}
