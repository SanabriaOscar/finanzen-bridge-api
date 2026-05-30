package com.finnazen.bridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FinnazenBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinnazenBridgeApplication.class, args);
    }
}
