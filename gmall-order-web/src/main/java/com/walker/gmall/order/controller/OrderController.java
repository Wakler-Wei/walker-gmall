package com.walker.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.walker.gmall.LoginRequire;
import com.walker.gmall.bean.*;
import com.walker.gmall.bean.enums.OrderStatus;
import com.walker.gmall.bean.enums.ProcessStatus;
import com.walker.gmall.service.CartService;
import com.walker.gmall.service.ManageService;
import com.walker.gmall.service.OrderService;
import com.walker.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/25 19:57
 * @Version 1.0
 */
@Controller
public class OrderController {
    @Reference
    UserInfoService userInfoService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    ManageService manageService;

    @RequestMapping("getAdress")
    public List<UserAddress> getAdressById(String userId){
        List<UserAddress> userAddressList = userInfoService.getUserAdressById(userId);
        return userAddressList;
    }


    @RequestMapping("trade")
    @LoginRequire
    public String tradeInit (HttpServletRequest request){
        //得到用户ID
        String userId = (String) request.getAttribute("userId");
        //得到用户选中的购物车
        List<CartInfo> cartList = cartService.getCartCheckedList(userId);
        //得到用户地址
        List<UserAddress> userAdressById = userInfoService.getUserAdressById(userId);
        //将用户地址传到前端
        request.setAttribute("userAddressList",userAdressById);

        //创建一个LIST来存储信息
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartList) {
            //每次循环先建一个OrderDetail来存储用户订单信息
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            //每次循环将数据放入LIST
            orderDetailList.add(orderDetail);
        }
        //将数据返回给前端
        request.setAttribute("orderDetailList",orderDetailList);
        //计算总价格
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        //返回给前端总价格
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        //生成流水号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);

        return "trade";
    }


    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){

        //获取用户ID
        String userId = (String) request.getAttribute("userId");

        //验证是否是当前订单
        String tradeNo = request.getParameter("tradeNo");
        // 调用比较方法
        boolean result = orderService.checkTradeNo(tradeNo, userId);
        if (result==false) {
            request.setAttribute("errMsg","请勿重复提交订单！");
            return "tradeFail";

        }
        // 删除缓存的流水号
        orderService.delTradeNo(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //验证库存
            boolean flag = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());

            if (flag == false) {


                request.setAttribute("errMsg","库存不足！！");
                return "tradeFail";
            }
            // 验证价格：orderDetail.getOrderPrice()== skuInfo.price
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            //
            int res = orderDetail.getOrderPrice().compareTo(skuInfo.getPrice());
            if (res!=0){
                request.setAttribute("errMsg",orderDetail.getSkuName()+"商品价格有变动，请重新下单！");
                // 加载最新价格到缓存！
                cartService.loadCartCache(userId);
                return "tradeFail";
            }

        }

        //初始化订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID);//订单状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID); //进度状态
        orderInfo.setUserId(userId);
        orderInfo.sumTotalAmount();   //订单总价格

        //保存订单
        String orderId = orderService.saveOrder(orderInfo);

        // 重定向
        return "redirect://payment.gmall.com/index?orderId="+orderId;

        //return JSON.toJSONString(orderInfo);

    }




}
