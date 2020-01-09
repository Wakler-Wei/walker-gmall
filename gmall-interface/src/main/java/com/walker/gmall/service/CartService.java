package com.walker.gmall.service;

import com.walker.gmall.bean.CartInfo;

import java.util.List;

/**
 * @Author Walker
 * @Date 2020/1/8 15:05
 * @Version 1.0
 */
public interface CartService {
    //添加购物车
    void addToCart(String skuId, String userId, int skuNum);
    /**
     * 根据userId 查询购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

}
