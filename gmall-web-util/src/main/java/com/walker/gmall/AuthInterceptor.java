package com.walker.gmall;

import com.alibaba.fastjson.JSON;
import com.walker.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

/**
 * springMVC拦截器
 * @Author Walker
 * @Date 2020/1/7 20:04
 * @Version 1.0
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    // 进入控制器之前执行！
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    //登录成功之后，得到一个newToken
    //https://www.jd.com/?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.dzzeXEhiqnKFvURLBqhDQpMW6mtuHh5W95wkLpkwY0E
        String token = request.getParameter("newToken");
        if (token != null) {
            //把token放入cookie中
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        //不能在地址栏获取token的时候
        if (token == null) {
            token = CookieUtil.getCookieValue(request, "token", false);
        }
        //执行完上面的程序之后怕判断token是否有值
        if (token != null) {
            //解密
            Map map = getUserMapByToken(token);
            String nickName = String.valueOf(map.get("nickName"));

            request.setAttribute("nickName",nickName);
            //测试
            //http://passport.atguigu.com/index?originUrl=http%3a%2f%2fitem.gmall.com%2f40.html
        }
        // 获取用户访问的控制器上是否有  注解 @LoginRequire
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //获取注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        //判断是否有这个注解
        if (methodAnnotation != null) {
            String salt = request.getHeader("X-forwarded-for");
            // 远程调用！
            //http://passport.atguigu.com/verify?token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.dzzeXEhiqnKFvURLBqhDQpMW6mtuHh5W95wkLpkwY0E&salt=192.168.111.1
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            if ("success".equals(result)){
               Map map = getUserMapByToken(token);
               String userId = (String) map.get("userId");
               request.setAttribute("userId",userId);
               return true;
            }else {
                // 当LoginRequire的注解中的属性autoRedirect =true 时必须登录！
                if (methodAnnotation.autoRedirect()){
                    // 应该跳转到登录页面！http://item.gmall.com/37.html -----> http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F37.html
                    // 得到用户访问的url 路径
                    String requestURL = request.getRequestURL().toString();
                    System.out.println(requestURL);// http://item.gmall.com/37.html
                    // 将 http://item.gmall.com/37.html 转换 http%3A%2F%2Fitem.gmall.com%2F37.html
                    String encodeURL  = URLEncoder.encode(requestURL, "UTF-8");
                    System.out.println(encodeURL);// http%3A%2F%2Fitem.gmall.com%2F37.html

                    // 重定向
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    // 拦截！
                    return false;
                }
            }
        }
        return true;
    }

    //解密token
    private Map getUserMapByToken(String token) {
        //newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.dzzeXEhiqnKFvURLBqhDQpMW6mtuHh5W95wkLpkwY0E
        //取token中的中间
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        //解密
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] decode = base64UrlCodec.decode(tokenUserInfo);
        //byte[] 转化成map 
        //先转化成String
        String tokenJson = new String(decode);
        return JSON.parseObject(tokenJson,Map.class);
    }


    // 进入控制器之后，返回视图之前执行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
    // 视图渲染之后，执行！
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
