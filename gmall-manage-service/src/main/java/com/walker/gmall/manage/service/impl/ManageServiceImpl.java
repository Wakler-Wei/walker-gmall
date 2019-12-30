package com.walker.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.walker.gmall.bean.*;
import com.walker.gmall.manage.mapper.*;
import com.walker.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    // 调用mapper
    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Resource  //默认查找type
    private  SpuInfoMapper spuInfoMapper;

    @Resource  // 默认按照name，如果没有name 找type
    private  BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Resource
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private  SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private  SkuImageMapper skuImageMapper;


    @Override
    public List<BaseCatalog1> getCatalog1() {
        // select * from basecatalog1 ;
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2) {
        //select * from baseCatalog2 where catalog1Id=?

        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3) {
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> attrInfoList(String catalog3Id) {
        /*Example example = new Example(BaseAttrInfo.class);
        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
        return baseAttrInfoMapper.selectByExample(example);*/
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            // 修改：
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        } else {
            // 直接保存平台属性
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        // baseAttrValue 修改：
        // 先将原有的数据删除，然后再新增！
        BaseAttrValue baseAttrValueDel = new BaseAttrValue();
        // delete from baseAttrValue where attrId = baseAttrInfo.getId();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        if (attrValueList != null && attrValueList.size() > 0) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }

        }

    }

    @Override
    public List<BaseAttrValue> getAttrValueList(BaseAttrValue baseAttrValue) {
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public List<BaseAttrValue> getBaseAttrInfo(BaseAttrValue baseAttrValue) {

        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(baseAttrValue.getAttrId());

        baseAttrInfo.setAttrValueList(getAttrValueList(baseAttrValue));

        return baseAttrInfo.getAttrValueList();
    }

    @Override
    public List<SpuInfo> getSpuInfoList(String catalog3Id) {
        Example example = new Example(SpuInfo.class);
        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
        return spuInfoMapper.selectByExample(example);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
         /*
            spuInfo
            spuImage
            spuSaleAttr
            spuSaleAttrValue
         */
        spuInfoMapper.insertSelective(spuInfo);
        // spuImage
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList!=null && spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                // 赋值spuId
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }
        // 销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList!=null && spuSaleAttrList.size()>0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                // 销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList!=null && spuSaleAttrValueList.size()>0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);

    }

    @Override
    public List<SpuImage> spuImageList(String spuId) {
        Example example = new Example(SpuImage.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        List<SpuImage> spuImages = spuImageMapper.selectByExample(example);
        return spuImages;
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {

        skuInfoMapper.insertSelective(skuInfo);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size()>0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size()>0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }

        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size()>0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }

        }

    }

}
