package com.doogoo.doogoo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
public class SemaphoreConfig {

    @Bean
    public Semaphore semaphore() {
        return new Semaphore(10);
    }
}
