package com.doogoo.doogoo.global.config;

import com.doogoo.doogoo.academic.domain.AcademicNotice;
import com.doogoo.doogoo.dodream.domain.DoDreamNotice;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public Cache<String, String> icsCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .maximumSize(100000)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, List<AcademicNotice>> academicNoticesCache() {
        return Caffeine.newBuilder()
                .maximumSize(1)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, List<DoDreamNotice>> doDreamNoticesCache() {
        return Caffeine.newBuilder()
                .maximumSize(1)
                .recordStats()
                .build();
    }
}
