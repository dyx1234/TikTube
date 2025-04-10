package com.azaz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author shigc
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.azaz.clients")
public class UserApplication {

    static {
        System.setProperty("druid.mysql.usePingMethod","false");
    }

     public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
