package com.walker.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.walker.gmall.CookieUtil;
import com.walker.gmall.LoginRequire;
import com.walker.gmall.bean.CartInfo;
import com.walker.gmall.bean.SkuInfo;
import com.walker.gmall.service.CartService;
import com.walker.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public  String cartList(HttpServletRequest request){

        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartInfoList = new ArrayList<>();
        //用户已经登陆
        if (userId != null) {
            cartInfoList  =cartService.getCartList(userId);
        }else {
            //获取cookie中的user-key
           String userTempId = CookieUtil.getCookieValue(request,"user-Key",false);

            if (userTempId != null) {
                cartInfoList =cartService.getCartList(userTempId);
            }
        }

        request.setAttribute("cartInfoList",cartInfoList);

        return "cartList";
    }


}
