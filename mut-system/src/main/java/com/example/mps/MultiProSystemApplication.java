package com.example.mps;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author pengYuJun
 */
@MapperScan("com.example.mps.mapper")
@SpringBootApplication
public class MultiProSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiProSystemApplication.class, args);
    }

}
