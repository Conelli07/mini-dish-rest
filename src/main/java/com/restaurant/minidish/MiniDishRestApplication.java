package com.restaurant.minidish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MiniDishRestApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiniDishRestApplication.class, args);
    }
}