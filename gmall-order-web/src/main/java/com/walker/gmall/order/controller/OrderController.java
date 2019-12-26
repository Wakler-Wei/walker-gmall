package com.walker.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.walker.gmall.bean.UserAddress;
import com.walker.gmall.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/25 19:57
 * @Version 1.0
 */
@RestController
public class OrderController {
    @Reference
    UserInfoService userInfoService;

    @RequestMapping("getAdress")
    public List<UserAddress> getAdressById(String userId){
        List<UserAddress> userAddressList = userInfoService.getUserAdressById(userId);
        return userAddressList;
    }


}
