package com.doogoo.doogoo.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sejong.portal")
public class SejongPortalConfig {

    private String loginUrl;
    private String calendarUrl;
    private String id;
    private String password;

    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }

    public String getCalendarUrl() { return calendarUrl; }
    public void setCalendarUrl(String calendarUrl) { this.calendarUrl = calendarUrl; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
