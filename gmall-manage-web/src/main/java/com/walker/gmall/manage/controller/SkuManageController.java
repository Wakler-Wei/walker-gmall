package com.walker.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.walker.gmall.bean.SkuInfo;
import com.walker.gmall.bean.SpuImage;
import com.walker.gmall.bean.SpuSaleAttr;
import com.walker.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/30 18:03
 * @Version 1.0
 */
@RestController
@CrossOrigin
public class SkuManageController {
    @Reference
    ManageService manageService;

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

    //saveSkuInfo
    @RequestMapping("saveSkuInfo")
    public  void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
    }
}
