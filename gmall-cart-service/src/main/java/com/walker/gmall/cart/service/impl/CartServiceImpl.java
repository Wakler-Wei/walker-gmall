package com.walker.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.walker.gmall.bean.CartInfo;
import com.walker.gmall.bean.SkuInfo;
import com.walker.gmall.cart.constant.CartConst;
import com.walker.gmall.cart.mapper.CartInfoMapper;
import com.walker.gmall.config.RedisUtil;
import com.walker.gmall.service.CartService;
import com.walker.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 * @Author Walker
 * @Date 2020/1/8 15:05
 * @Version 1.0
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;


    @Override
    public void addToCart(String skuId, String userId, int skuNum) {
          /*
        1.  先查看数据库中是否有该商品
            select * from cartInfo where userId = ? and skuId = ?
            true: 数量相加upd
            false: 直接添加
        2.  放入redis！
        */
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();

        // 定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        // 调用查询数据库并加入缓存
        if(!jedis.exists(cartKey)){
            loadCartCache(userId);
        }
        //将数据添加到数据库
        //先将数据查出来
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userId).andEqualTo("skuId",skuId);
        CartInfo cartInfoExist = cartInfoMapper.selectOneByExample(example);
        //购物车中有该商品
        if (cartInfoExist != null) {
            //更新数量
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            //更新商品的价格（从商品详情中获取价格）
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            //更新数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);

        }else {
            //购物车没有 该商品
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);

            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            //添加到到数据库
            cartInfoMapper.insertSelective(cartInfo);

            cartInfoExist = cartInfo;

        }
        //放到缓存中
        jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist));
        //设置过期时间
        setCartKeyExpire(userId,jedis,cartKey);

        jedis.close();

    }

    /**
     * 购物车列表
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {
         /*
        1.  获取redis中的购物车数据
        2.  如果redis 没有，从mysql 获取并放入缓存
         */
        List<CartInfo> cartInfoList = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();

        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //获取购物车中的value
        List<String> stringList  = jedis.hvals(cartKey);

        if (stringList != null && stringList.size()>0) {
            for (String cartJson : stringList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);

            }
            // 自定义比较器
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
//            stream() ; 开发经常使用！
//            jdk1.8
//            Collections.sort(cartInfoList,(CartInfo o1, CartInfo o2) -> o1.getId().compareTo(o2.getId()));


            return cartInfoList;
        }else {
            cartInfoList=loadCartCache(userId);
            return cartInfoList;

        }
    }


    // 获取数据库中的数据并放入缓存
    public List<CartInfo> loadCartCache(String userId){
        //从数据库中查数据
       List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
       //cartkey
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        if (cartInfoList == null && cartInfoList.size()<=0) {
            return null;
        }
       Jedis jedis = redisUtil.getJedis();

        Map<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {

            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));

        }
        jedis.hmset(cartKey,map);
        jedis.close();

        return cartInfoList;
    }


    /**
     * 设置过期时间
     * @param userId
     * @param jedis
     * @param cartKey
     */
    private void setCartKeyExpire(String userId, Jedis jedis, String cartKey) {
        // 设置过期时间 key = { 根据用户的购买力！ 根据用户的过期时间设置购物车的过期时间}
        // 获取用户的key
        String userKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;

        if (jedis.exists(userKey)){
            // 获取用户key 的过期时间
            Long ttl = jedis.ttl(userKey);

            // 将用户的过期时间给购物车的过期时间
            jedis.expire(cartKey,ttl.intValue());
        }else {
            jedis.expire(cartKey,7*24*3600);
        }
    }
}
