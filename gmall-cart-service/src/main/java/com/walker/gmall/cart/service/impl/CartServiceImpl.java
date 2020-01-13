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
    public void addToCart(String skuId, String userId, Integer skuNum) {
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
        //
        //        // 调用查询数据库并加入缓存
        //        if(!jedis.exists(cartKey)){
        //            loadCartCache(userId);
        //        }
        //        //将数据添加到数据库
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

    /**
     * 合并购物车数据
     * @param cartInfoNoLoginList
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList, String userId) {
        //新的传给controller层的集合
        List<CartInfo> cartInfoList = new ArrayList<>();
        //登陆时的数据
        List<CartInfo> cartInfoLoginList = cartInfoMapper.selectCartListWithCurPrice(userId);
/*
        demo1:
            登录：
                37 1
                38 1
            未登录：
                37 1
                38 1
                39 1
            合并之后的数据
                37 2
                38 2
                39 1
         demo2:
             未登录：
                37 1
                38 1
                39 1
                40 1
              合并之后的数据
                37 1
                38 1
                39 1
                40 1
        */
        //demo1
        //登录用户购物车不为空
        if (cartInfoLoginList != null &&cartInfoLoginList.size()>0) {
            //遍历登录的购物车

            for (CartInfo cartInfoNoLogin : cartInfoNoLoginList) {
                boolean isFalse = false;
                //遍历未登录的购物车
                for (CartInfo cartInfoLogin : cartInfoLoginList) {
                    //根据商品ID合并购物车
                    if (cartInfoLogin.getSkuId().equals(cartInfoNoLogin.getSkuId())) {
                        //数量
                        cartInfoLogin.setSkuNum(cartInfoLogin.getSkuNum() + cartInfoNoLogin.getSkuNum());
                        //把数据更新到数据库中
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoLogin);

                        isFalse=true;
                    }
                }
                //没有相同商品
                if(!isFalse){
                    {
                        //把主键设置为NULL，主键自增
                        cartInfoNoLogin.setId(null);
                        //把userID设置为登录用户的id
                        cartInfoNoLogin.setUserId(userId);

                        cartInfoMapper.insertSelective(cartInfoNoLogin);
                    }
                }
            }
         //登录用户购物车为空 demo2 把未登录用户
        }else {
            for (CartInfo cartInfo : cartInfoNoLoginList) {
                //把主键设置为NULL，主键自增
                cartInfo.setId(null);
                //把userID设置为登录用户的id
                cartInfo.setUserId(userId);
                //向数据库中插入数据
                cartInfoMapper.insertSelective(cartInfo);
            }
        }
        //把数据库中数据放入缓存，并赋值
        cartInfoList = loadCartCache(userId);
        if (cartInfoList != null && cartInfoList.size()>0) {
            for (CartInfo cartInfo : cartInfoList) {
                for (CartInfo cartInfoNoLogin : cartInfoNoLoginList) {
                    //商品相同
                    if (cartInfo.getSkuId().equals(cartInfoNoLogin.getSkuId())) {
                        if ("1".equals(cartInfoNoLogin.getIsChecked())) {
                            cartInfo.setIsChecked("1");
                            checkCart(cartInfoNoLogin.getSkuId(),userId,"1");

                        }
                    }
                }
            }
        }

        return cartInfoList;
    }

    /**
     * 删除购物车中的临时数据
     * @param userTempId
     */
    @Override
    public void deleteCartList(String userTempId) {
        //删除数据库
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userTempId);
        cartInfoMapper.deleteByExample(example);

        //删除缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX+userTempId+CartConst.USER_CART_KEY_SUFFIX;
        jedis.del(cartKey);
        jedis.close();

    }

    /**
     * 改变选中状态
     * @param skuId
     * @param userId
     * @param isChecked
     */
    @Override
    public void checkCart(String skuId, String userId, String isChecked) {
        //// 方案：第一种：修改mysql，redis
        Jedis jedis = redisUtil.getJedis();
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        Example example = new Example(cartInfo.getClass());
        example.createCriteria().andEqualTo("skuId",skuId).andEqualTo("userId",userId);
        // 第一个参数表示修改的内容 第二个参数 example 永远都代表查询，更新，删除等的条件
        cartInfoMapper.updateByExampleSelective(cartInfo,example);

        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        // 获取到当前的商品
        String cartInfoJson = jedis.hget(cartKey, skuId);
        //转换成对象
        CartInfo cartInfoNew = JSON.parseObject(cartInfoJson, CartInfo.class);
        cartInfoNew.setIsChecked(isChecked);
        jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfoNew));

        jedis.close();


    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {

        ArrayList<CartInfo> infoArrayList = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();

        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        List<String> cartCheckedList = jedis.hvals(cartKey);
        if (cartCheckedList != null && cartCheckedList.size()>0) {

            for (String cartJson : cartCheckedList) {

                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                if ("1".equals(cartInfo.getIsChecked())) {

                    infoArrayList.add(cartInfo);

                }

            }
            
        }

        return infoArrayList;
    }


    // 获取数据库中的数据并放入缓存
    public List<CartInfo> loadCartCache(String userId){
        //从数据库中查数据
       List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
       //cartkey
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        if (cartInfoList == null || cartInfoList.size()==0) {
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
