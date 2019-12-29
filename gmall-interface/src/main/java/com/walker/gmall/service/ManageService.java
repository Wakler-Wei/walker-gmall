package com.walker.gmall.service;

import com.walker.gmall.bean.*;

import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/27 18:58
 * @Version 1.0
 */
public interface ManageService {
    List<BaseCatalog1> getCatalog1();

    List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2);

    List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3);
    //属性列表
    List<BaseAttrInfo> attrInfoList(BaseAttrInfo baseAttrInfo);
    //添加属性与属性值
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    //功能实现修改回显属性值
    List<BaseAttrValue> getAttrValueList(BaseAttrValue baseAttrValue);
    //业务实现修改回显属性值
    List<BaseAttrValue> getBaseAttrInfo(BaseAttrValue baseAttrValue);
    //查询商品SPU属性
    List<SpuInfo> getSpuInfoList(String catalog3Id);
    //查询商品销售属性列表
    List<BaseSaleAttr> getBaseSaleAttrList();
    //保存
    void saveSpuInfo(SpuInfo spuInfo);
    //根据ID查询商品销售属性列表
    List<SpuSaleAttr> spuSaleAttrList(String spuId);
    //根据ID查询商品图片列表
    List<SpuImage> spuImageList(String spuId);



}
