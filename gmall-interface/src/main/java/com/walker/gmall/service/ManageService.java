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

    //平台属性列表
    List<BaseAttrInfo> attrInfoList(String catalog3Id);

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
    //保存Spu
    void saveSpuInfo(SpuInfo spuInfo);

    //根据ID查询商品销售属性列表
    List<SpuSaleAttr> spuSaleAttrList(String spuId);
    //根据ID查询商品图片列表
    List<SpuImage> spuImageList(String spuId);

    //保存SKU
    void saveSkuInfo(SkuInfo skuInfo);
    //根据主键查询商品详情
    SkuInfo getSkuInfo(String skuId);
    //根据SKUINFO查询商品的销售属性与销售属性值
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);
    //查询 销售属性值与skuId 组合的数据集合
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);


    //SkuLsResult中的attrValueIdList  集合查询平台属性
    List<BaseAttrInfo> attrInfoList(List<String> attrValueIdList);
}
