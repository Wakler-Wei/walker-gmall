package com.walker.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.walker.gmall.bean.*;
import com.walker.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/27 19:01
 * @Version 1.0
 */
//@Controller
@RestController
@CrossOrigin
public class ManageController {
    @Reference
    ManageService manageService;


    /**
     * 一级列表
     * @return
     */
    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1(){
        return manageService.getCatalog1();
    }

    /**
     * 二级列表
     * @param baseCatalog2
     * @return
     */
    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2){
        return manageService.getCatalog2(baseCatalog2);
    }

    /**
     * 三级列表
     * @param baseCatalog3
     * @return
     */
    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3){
        return manageService.getCatalog3(baseCatalog3);
    }

    /**
     * 商品属性
     * @return
     */
    @RequestMapping("attrInfoList")
    public  List<BaseAttrInfo> attrInfoList(String catalog3Id){
        //return manageService.attrInfoList(baseAttrInfo);
        return manageService.attrInfoList(catalog3Id);
    }

    /**
     * 添加
     * @param baseAttrInfo
     */
    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
    }
    /**
     * 修改回显（商品属性值）
     */
    @RequestMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(BaseAttrValue baseAttrValue){

        //功能
        //return manageService.getAttrValueList(baseAttrValue);
        //业务
        return  manageService.getBaseAttrInfo(baseAttrValue);
    }

}
