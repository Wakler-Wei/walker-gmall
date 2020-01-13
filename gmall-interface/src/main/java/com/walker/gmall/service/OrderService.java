package com.walker.gmall.service;

import com.walker.gmall.bean.OrderInfo;

/**
 * @Author Walker
 * @Date 2020/1/12 15:29
 * @Version 1.0
 */
public interface OrderService {
    //订单保存
    String saveOrder(OrderInfo orderInfo);
    //流水号
    String getTradeNo(String userId);
    //验证流水号
    boolean checkTradeNo(String tradeNo, String userId);
    //删除REDIS中的流水号
    void delTradeNo(String userId);
    //验证库存
    boolean checkStock(String skuId, Integer skuNum);
    //获取订单信息
    OrderInfo getOrderInfo(String orderId);
}
