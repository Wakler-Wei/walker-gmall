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
    void addToCart(String skuId, String userId, Integer skuNum);
    /**
     * 根据userId 查询购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 合并购物车
     * @param cartInfoNoLoginList
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList, String userId);

    /**
     * 删除未登录购物车数据
     * @param userTempId
     */
    void deleteCartList(String userTempId);

    /**
     * 改变选中状态
     * @param skuId
     * @param userId
     * @param isChecked
     */
    void checkCart(String skuId, String userId, String isChecked);

    /**
     * 查询用户购物车中选中的商品
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);

    /**
     * 查最新价格
     * @param userId
     * @return
     */
    List<CartInfo> loadCartCache(String userId);
}
