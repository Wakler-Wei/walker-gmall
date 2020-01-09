package com.walker.gmall.cart.mapper;

import com.walker.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author Walker
 * @Date 2020/1/8 15:06
 * @Version 1.0
 */
public interface CartInfoMapper extends Mapper<CartInfo> {
    // 根据用户Id 查询购物车数据
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
