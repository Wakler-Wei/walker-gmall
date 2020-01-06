package com.walker.gmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // xxx.xml
public class RedisConfig {

    /*
    1.  获取到配置文件中的host,port,timeOut 等参数、
    2.  将RedisUtil 放入到spring 容器中管理
     */
    // 获取配置文件中的host。如果host没有数据，则给一个默认值 ：【disabled】
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;


    @Value("${spring.redis.timeOut:10000}")
    private int timeOut;

    /*
    <bean name="redisUtil"  class="com.atguigu.gmall0715.config.RedisUtil">
    </bean>
     */
    @Bean
    public RedisUtil getRedisUtil(){
        // 如果没有host 则返回一个空对象
        if ("disabled".equals(host)){
            return null;
        }

        RedisUtil redisUtil = new RedisUtil();
        // 初始化连接池工厂
        redisUtil.initJedisPool(host,port,timeOut);

        return redisUtil;
    }







}
