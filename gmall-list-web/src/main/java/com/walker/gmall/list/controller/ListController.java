package com.walker.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.walker.gmall.bean.SkuLsParams;
import com.walker.gmall.bean.SkuLsResult;
import com.walker.gmall.service.ListService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Walker
 * @Date 2020/1/5 18:58
 * @Version 1.0
 */
@RestController
public class ListController {

    // http://list.gmall.com/list.html?catalog3Id=61

    @Reference
    private ListService listService;

    @RequestMapping("list.html")
    @ResponseBody
    public String list(SkuLsParams skuLsParams){
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        return JSON.toJSONString(skuLsResult);
    }
}
