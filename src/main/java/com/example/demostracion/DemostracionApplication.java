package com.example.demostracion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemostracionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemostracionApplication.class, args);
    }

}