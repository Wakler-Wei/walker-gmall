package com.walker.gmall.service;

import com.walker.gmall.bean.PaymentInfo;

/**
 * @Author Walker
 * @Date 2020/1/12 21:08
 * @Version 1.0
 */
public interface PaymentService {
    // 保存记录{void}并生成二维码{返回页面String} PaymentInfo
    void savyPaymentInfo(PaymentInfo paymentInfo);

    // // 根据outTradeNo 查询数据
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    // 更新交易记录中的状态
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    //退款
    boolean refund(String orderId);
}
