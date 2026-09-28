package com.codeplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CodePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodePlatformApplication.class, args);
    }
}
