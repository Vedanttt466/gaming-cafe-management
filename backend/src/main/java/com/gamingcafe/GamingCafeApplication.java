package com.gamingcafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GamingCafeApplication {
    public static void main(String[] args) {
        SpringApplication.run(GamingCafeApplication.class, args);
    }
}
