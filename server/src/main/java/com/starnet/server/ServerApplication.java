package com.starnet.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Autowired
    RedisConnectionFactory factory;

    /**
     * 应用关闭时清空redis数据库
     */
//    @PreDestroy
//    public void flushDb(){
//        factory.getConnection().flushDb();
//    }
}
