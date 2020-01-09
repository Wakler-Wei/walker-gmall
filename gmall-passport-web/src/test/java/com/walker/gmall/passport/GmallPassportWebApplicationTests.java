package com.walker.gmall.passport;

import com.walker.gmall.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {



    @Test
    public static void main(String[] args) {
        String key = "walker";
        String salt = "192.168.111.131";
        Map map = new HashMap();
        map.put("userId", "1001");
        map.put("nickName", "marry");
        //加密
        String token = JwtUtil.encode(key, map, salt);
        System.err.println(token);
        //同一网络节点下也可以解密
        //需要修改工具类JwtUtil
        ////将最新的key进行加密
        //        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        //        key = base64UrlCodec.encode(key);
        Map<String, Object> decode1 = JwtUtil.decode(token, key, salt);
        Map<String, Object> decode = JwtUtil.decode(token, key, "192.168.111.102");
        Map<String, Object> decode2 = JwtUtil.decode(token, key, "192.168.11.102");
        System.out.println(decode1);
        System.out.println(decode);
        System.out.println(decode2);

        //消除-
        //消除-
        String uuid1 = UUID.randomUUID().toString();
        System.err.println(uuid1);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println(uuid);

    }




}

