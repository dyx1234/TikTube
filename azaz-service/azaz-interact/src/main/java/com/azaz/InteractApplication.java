package com.azaz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 互动服务启动类
 * @author shigc
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.azaz.clients")
@EnableTransactionManagement
public class InteractApplication {
    public static void main(String[]args){
        SpringApplication.run(InteractApplication.class);
    }
}
