package com.saimone.full_fledged_rest_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class FullFledgedRestAPIApplication {
    public static void main(String[] args) {
        SpringApplication.run(FullFledgedRestAPIApplication.class, args);
    }
}