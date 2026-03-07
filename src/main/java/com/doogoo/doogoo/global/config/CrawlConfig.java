package com.doogoo.doogoo.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "crawl.dodream")
public class CrawlConfig {

    private String baseUrl;
    private String listPath;
    private String detailPath;
    private long delayMs;
    private int regularPages;
    private List<String> activeStatuses;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getListPath() { return listPath; }
    public void setListPath(String listPath) { this.listPath = listPath; }

    public String getDetailPath() { return detailPath; }
    public void setDetailPath(String detailPath) { this.detailPath = detailPath; }

    public long getDelayMs() { return delayMs; }
    public void setDelayMs(long delayMs) { this.delayMs = delayMs; }

    public int getRegularPages() { return regularPages; }
    public void setRegularPages(int regularPages) { this.regularPages = regularPages; }

    public List<String> getActiveStatuses() { return activeStatuses; }
    public void setActiveStatuses(List<String> activeStatuses) { this.activeStatuses = activeStatuses; }

    public String buildListUrl(int page) {
        return baseUrl + listPath.replace("{page}", String.valueOf(page));
    }

    public String buildListUrl(int page, String status) {
        return baseUrl + listPath.replace("{page}", String.valueOf(page)) + "?status=" + status;
    }

    public String buildDetailUrl(long id) {
        return baseUrl + detailPath.replace("{id}", String.valueOf(id));
    }
}
