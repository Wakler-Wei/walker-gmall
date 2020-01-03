package com.walker.gmall.manage.mapper;

import com.walker.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/30 18:40
 * @Version 1.0
 */
public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
