package com.nacai.settlement.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by shan on 2018/5/29.
 */
@SpringBootApplication
@MapperScan("com.nacai.settlement.server.dao")
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ServerApplication.class);
        springApplication.run(args);
    }
}
