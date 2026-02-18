package com.doogoo.doogoo.common.url;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class PublicBaseUrlResolver {

    @Value("${public.base.url:}")
    private String publicBaseUrl;

    public String resolveBaseUrl(HttpServletRequest request) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.trim().replaceAll("/+$", "");
        }
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("")
                .build()
                .toUriString();
    }
}
