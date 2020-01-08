package com.walker.gmall.passport.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.Base64UrlCodec;

import java.util.Map;

public class JwtUtil {
    public static String encode(String key,Map<String,Object> param,String salt){
        if(salt!=null){
            key+=salt;
        }
        //将最新的key进行加密
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        key = base64UrlCodec.encode(key);

        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }


    public  static Map<String,Object> decode(String token , String key, String salt){
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        //将最新的key进行加密
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        key = base64UrlCodec.encode(key);
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
            return null;
        }
        return  claims;
    }

}
