package com.walker.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.walker.gmall.CookieUtil;
import com.walker.gmall.LoginRequire;
import com.walker.gmall.bean.CartInfo;
import com.walker.gmall.bean.SkuInfo;
import com.walker.gmall.service.CartService;
import com.walker.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author Walker
 * @Date 2020/1/8 15:04
 * @Version 1.0
 */
@Controller
public class CartController {
    @Reference
    CartService cartService;

    @Reference
    ManageService manageService;

    /**
     * 添加购物车
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)    //获取useId  只有有这个注解的时候request域中才会存useId
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        String skuNum = request.getParameter("num");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");
        //如果没有获取到userId
        if (userId == null) {
            // 用户未登录 存储一个临时的用户Id，存储在cookie 中!
            userId = CookieUtil.getCookieValue(request, "user-Key", false);
            // 说明未登录情况下，根本没有添加过一件商品
            if (userId == null) {
                //创建一个临时的用户ID
                ////消除-
                //String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                userId = UUID.randomUUID().toString().replace("-", "");
                //将生成的UUID放入缓存
                CookieUtil.setCookie(request,response,"user-Key",userId,7*24*3600,false);
            }
        }
        cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));

        //制作页面渲染需要的数据
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";

    }

    /**
     * 购物车列表
     * @param request
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public  String cartList(HttpServletRequest request){
        //从request域中获取userId
        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartInfoList = new ArrayList<>();
        //用户已经登陆
        if (userId != null) {
            // 从缓存中获取购物车数据列表
            // 查询未登录是否有购物车数据
            // 从cookie 中获取临时的userId
            String userTempId = CookieUtil.getCookieValue(request,"user-Key",false);

            List<CartInfo> cartInfoNoLoginList = new ArrayList<>();
            if (!StringUtils.isEmpty(userTempId)) {
                //获取未登录状态下的购物车
             cartInfoNoLoginList =cartService.getCartList(userTempId);

                if (cartInfoNoLoginList != null && cartInfoNoLoginList.size()>0) {
                    //将购物车合并
                    cartInfoList = cartService.mergeToCartList(cartInfoNoLoginList,userId);
                    //删除未登录的购物车
                    cartService.deleteCartList(userTempId);
                }
            }

            if(StringUtils.isEmpty(userTempId) || (cartInfoNoLoginList == null || cartInfoNoLoginList.size()==0)){
                // 说明未登录没有数据， 直接获取数据库！
                cartInfoList  =cartService.getCartList(userId);

            }

        //用户未登录
        }else {
            //获取cookie中的user-key
           String userTempId = CookieUtil.getCookieValue(request,"user-Key",false);

            if (!StringUtils.isEmpty(userTempId)) {
                cartInfoList =cartService.getCartList(userTempId);
            }
        }

        request.setAttribute("cartInfoList",cartInfoList);

        return "cartList";
    }

    @RequestMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(HttpServletRequest request){
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");

        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId= CookieUtil.getCookieValue(request,"user-Key",false);
        }
        cartService.checkCart(skuId,userId,isChecked);

    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request){

        String userId = (String) request.getAttribute("userId");

        // 未登录
        String userTempId = CookieUtil.getCookieValue(request,"user-Key",false);

        if (!StringUtils.isEmpty(userTempId)) {

            List<CartInfo> cartNoInfoList = cartService.getCartList(userTempId);

            if (cartNoInfoList != null && cartNoInfoList.size()>0) {
                //合并购物车
                cartService.mergeToCartList(cartNoInfoList,userId);
                //删除未登录数据
                cartService.deleteCartList(userTempId);
            }
        }

        return "redirect://trade.gmall.com/trade";
    }


}
