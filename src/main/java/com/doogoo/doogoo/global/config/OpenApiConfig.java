package com.doogoo.doogoo.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("두구두구 API")
                        .version("1.0")
                        .description("학사/두드림 공지 ICS 캘린더 구독 및 조회 API"))
                .servers(List.of(
                        new Server().url("https://www.sejongdoogoo-api.com").description("배포 서버"),
                        new Server().url("http://localhost:8080").description("로컬 서버")
                ));
    }
}
