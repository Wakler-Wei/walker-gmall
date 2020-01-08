package com.walker.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.walker.gmall.bean.UserInfo;
import com.walker.gmall.passport.config.JwtUtil;
import com.walker.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Walker
 * @Date 2020/1/7 10:07
 * @Version 1.0
 */

@Controller
public class PossPortController {

    @Reference
    UserInfoService userInfoService;

    @Value("token.key")
    private String key;

    /**
     * 登录
     * @param request
     * @return
     */
    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        // 保存上
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    /**
     * 登录生成token
     * @param userInfo
     * @param request
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){

        String salt = request.getHeader("X-forwarded-for");//192.168.111.1

        UserInfo info = userInfoService.login(userInfo);
        if (info != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());
            String token = JwtUtil.encode(key, map, salt);

            return token;
            //http://passport.atguigu.com/index?originUrl=https%3A%2F%2Fwww.jd.com%2F
            //eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.dzzeXEhiqnKFvURLBqhDQpMW6mtuHh5W95wkLpkwY0E
            //http://localhost:8087/index?originUrl=https%3A%2F%2Fwww.jd.com%2F
            //eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.s-G4oHrC-1I1aICXdVMESgU4i5pzROe3jXMJIOEcxpY
        }
        return "fail";
    }

    /**
     * 解密
     * @param request
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        //获取token
        String token = request.getParameter("token");
        //获取
        String salt = request.getParameter("salt");
        //解密
        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        if (map != null) {
            //得到useId
            String userId = String.valueOf(map.get("userId"));

            UserInfo userInfo = userInfoService.verify(userId);
            if (userInfo != null) {
                return "success";
            }
        }
        return "fail";
    }
}
