package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.config.CrawlConfig;
import com.doogoo.doogoo.exception.CustomException;
import com.doogoo.doogoo.exception.ErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DodreamCrawler {

    private static final Logger log = LoggerFactory.getLogger(DodreamCrawler.class);
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final CrawlConfig config;

    public DodreamCrawler(CrawlConfig config) {
        this.config = config;
    }

    public Document fetchListPage(int page) {
        String url = config.buildListUrl(page);
        log.info("목록 페이지 크롤링: {}", url);
        return fetch(url);
    }

    public Document fetchListPage(int page, String status) {
        String url = config.buildListUrl(page, status);
        log.info("목록 페이지 크롤링 (status={}): {}", status, url);
        return fetch(url);
    }

    public Document fetchDetailPage(long dodreamId) {
        String url = config.buildDetailUrl(dodreamId);
        log.info("상세 페이지 크롤링: dodreamId={}", dodreamId);
        return fetch(url);
    }

    private Document fetch(String url) {
        delay();
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10_000)
                    .get();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.CRAWL_FAILED, e);
        }
    }

    private void delay() {
        try {
            Thread.sleep(config.getDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
