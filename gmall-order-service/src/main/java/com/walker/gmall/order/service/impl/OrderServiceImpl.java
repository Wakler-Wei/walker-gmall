package com.walker.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.walker.gmall.bean.OrderDetail;
import com.walker.gmall.bean.OrderInfo;
import com.walker.gmall.config.RedisUtil;
import com.walker.gmall.order.mapper.OrderDetailMapper;
import com.walker.gmall.order.mapper.OrderInfoMapper;
import com.walker.gmall.service.OrderService;
import com.walker.gmall.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * @Author Walker
 * @Date 2020/1/12 15:28
 * @Version 1.0
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public String saveOrder(OrderInfo orderInfo) {
        //设置订单创建时间
        orderInfo.setCreateTime(new Date());
        //设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());

        //设置第三方支付编码
        String outTradeNo = "Walker" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //保存到数据库
        orderInfoMapper.insertSelective(orderInfo);

        //插入订单详情信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }

        //订单号
        String orderId = orderInfo.getId();

        return orderId;
    }

    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        // 生成一个流水号
        String tradeCode= UUID.randomUUID().toString().replace("-","");

        jedis.set(tradeNoKey,tradeCode);
        jedis.close();

        return tradeCode;
    }

    @Override
    public boolean checkTradeNo(String tradeNo, String userId) {

        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey="user:"+userId+":tradeCode";

        String redisTradeNo = jedis.get(tradeNoKey);
        jedis.close();

        return tradeNo.equals(redisTradeNo);
    }

    @Override
    public void delTradeNo(String userId) {

        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey="user:"+userId+":tradeCode";

        jedis.del(tradeNoKey);

        jedis.close();

    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {

        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        return orderInfo;
    }
}
