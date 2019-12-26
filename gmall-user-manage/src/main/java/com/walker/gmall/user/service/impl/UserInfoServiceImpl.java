package com.walker.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.walker.gmall.bean.UserAddress;
import com.walker.gmall.bean.UserInfo;
import com.walker.gmall.service.UserInfoService;
import com.walker.gmall.user.mapper.UserAdressMapper;
import com.walker.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/25 18:27
 * @Version 1.0
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAdressMapper userAdressMapper;

    @Override
    public List<UserInfo> getList() {
        List<UserInfo> userInfoList = userInfoMapper.selectAll();
        return userInfoList;
    }

    @Override
    public List<UserAddress> getUserAdressById(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAdressMapper.select(userAddress);

        /*Example example = new Example(UserAdress.class);
        example.createCriteria().andEqualTo("userId",userId);
        return userAdressMapper.selectByExample(example);*/

    }
}
