package com.doogoo.doogoo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
public class SemaphoreConfig {
    
    @Value("${app.concurrency.semaphore-count:10}")
    private int semaphoreCount;

    @Bean
    public Semaphore semaphore() {
        return new Semaphore(semaphoreCount);
    }
}
