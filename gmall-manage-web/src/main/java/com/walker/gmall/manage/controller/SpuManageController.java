package com.walker.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.walker.gmall.bean.BaseSaleAttr;
import com.walker.gmall.bean.SpuImage;
import com.walker.gmall.bean.SpuInfo;
import com.walker.gmall.bean.SpuSaleAttr;
import com.walker.gmall.service.ManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Walker
 * @Date 2019/12/29 16:53
 * @Version 1.0
 */
@RestController
@CrossOrigin
public class SpuManageController {
    @Reference
    ManageService manageService;

    //spuList
    @RequestMapping("spuList")
    public List<SpuInfo> getSpuInfoList(String catalog3Id){
        return manageService.getSpuInfoList(catalog3Id);
    }

    // http://localhost:8082/baseSaleAttrList
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }

    // http://localhost:8082/saveSpuInfo
    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
    }
    //spuSaleAttrList
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){
        return  manageService.spuSaleAttrList(spuId);

    }

    //spuImageList
    @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(String spuId){
       return manageService.spuImageList(spuId);
    }
}
