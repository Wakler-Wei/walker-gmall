package com.walker.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.walker.gmall.bean.UserAddress;
import com.walker.gmall.bean.UserInfo;
import com.walker.gmall.config.RedisUtil;
import com.walker.gmall.service.UserInfoService;
import com.walker.gmall.user.mapper.UserAdressMapper;
import com.walker.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

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

    @Autowired
    RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24*7;


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

    @Override
    public UserInfo login(UserInfo userInfo) {
        //对用户密码进行MD5加密
        String passwd = userInfo.getPasswd();
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPasswd);
        //根据userInfo查询数据库是否存在该用户
        UserInfo info = userInfoMapper.selectOne(userInfo);
        //如果该用户存在，把该用户信息存储到redis中
        if (info != null) {

            Jedis jedis = redisUtil.getJedis();
            String useKey = userKey_prefix + info.getId() + userinfoKey_suffix;
            jedis.setex(useKey,userKey_timeOut, JSON.toJSONString(info));

            jedis.close();
            return info;

        }

        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        //获取Jedis
        Jedis jedis = redisUtil.getJedis();
        //组成useKey
        String useKey = userKey_prefix + userId + userinfoKey_suffix;
        //查找redis缓存中是否有该数据
        String useJson = jedis.get(useKey);
        //如果有该数据
        if (useJson != null) {
            //把这个useJson转换成UserInfo
            UserInfo userInfo = JSON.parseObject(useJson, UserInfo.class);
            return userInfo;
        }
        return null;
    }
}
