package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.global.config.CrawlConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DodreamCrawler {
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final CrawlConfig config;

    public DodreamCrawler(CrawlConfig config) {
        this.config = config;
    }

    public Document fetchListPage(int page, String status) {
        String url = config.buildListUrl(page, status);
        return fetch(url);
    }

    public Document fetchDetailPage(long dodreamId) {
        String url = config.buildDetailUrl(dodreamId);
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
            throw new DoogooException(ErrorCode.CRAWL_FAILED, e);
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
