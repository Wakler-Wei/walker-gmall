package com.walker.gmall.user.controller;

import com.walker.gmall.bean.UserInfo;
import com.walker.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/25 18:31
 * @Version 1.0
 */
@RestController
public class UserInfoController {
    @Autowired
    UserInfoService userInfoService;

    /**
     * 获取用户列表
     * @return
     */
    @RequestMapping("getList")
    public List<UserInfo> getList(){
        return userInfoService.getList();
    }

}
