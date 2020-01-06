package com.walker.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.walker.gmall.bean.SkuInfo;
import com.walker.gmall.bean.SkuLsInfo;
import com.walker.gmall.bean.SpuImage;
import com.walker.gmall.bean.SpuSaleAttr;
import com.walker.gmall.service.ListService;
import com.walker.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
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

    @Reference
    ListService listService;

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
        //制作完sku之后上传到elasticsearch
        onSale(skuInfo.getId());
    }

    // 如何上传？ 根据skuId 来上传
    // 单个上传！
    @RequestMapping("onSale")
    public void onSale(String skuId){
        // 商品上架{saveSkuLsInfo}
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        // 给skuLsInfo 初始化赋值
        // 根据skuId 查询skuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        // 属性拷贝
        BeanUtils.copyProperties(skuInfo,skuLsInfo);
        listService.saveSkuLsInfo(skuLsInfo);
    }
}
