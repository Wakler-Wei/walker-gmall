package com.walker.gmall.service;


import com.walker.gmall.bean.SkuLsInfo;
import com.walker.gmall.bean.SkuLsParams;
import com.walker.gmall.bean.SkuLsResult;

public interface ListService {

    /**
     * 商品上架
     * @param skuLsInfo
     */
    void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    /**
     * 根据用户输入的检索条件查询数据
     * @param skuLsParams
     * @return
     */
    SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 更新es的hotScore
     * @param skuId
     */
    void incrHotScore(String skuId);
}
