package com.walker.gmall.service;

import com.walker.gmall.bean.UserAddress;
import com.walker.gmall.bean.UserInfo;

import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/25 18:26
 * @Version 1.0
 */
public interface UserInfoService {

    /**
     * 查询用户列表
     * @return
     */
    List<UserInfo> getList();

    /**
     * 根据用户ID查询用户地址
     * @param userId
     * @return
     */
    List<UserAddress> getUserAdressById(String userId);

}
