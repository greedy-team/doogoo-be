package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.global.config.SejongPortalConfig;
import jakarta.annotation.PostConstruct;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

@Component
public class AcademicCrawler {

    private static final Logger log = LoggerFactory.getLogger(AcademicCrawler.class);
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String PORTAL_BASE = "https://portal.sejong.ac.kr";

    private final SejongPortalConfig config;
    private SSLSocketFactory legacyTlsFactory;

    public AcademicCrawler(SejongPortalConfig config) {
        this.config = config;
    }

    @PostConstruct
    private void initLegacyTls() {
        // Java 17 기본 비활성화된 TLSv1, TLSv1.1을 세종대 포털 연결을 위해 허용
        String disabled = Security.getProperty("jdk.tls.disabledAlgorithms");
        if (disabled != null) {
            disabled = disabled.replaceAll("\\bTLSv1\\.1\\b,?\\s*", "")
                               .replaceAll("\\bTLSv1\\b,?\\s*", "")
                               .replaceAll(",\\s*$", "")
                               .trim();
            Security.setProperty("jdk.tls.disabledAlgorithms", disabled);
            log.info("세종대 포털 연결을 위해 레거시 TLS 활성화");
        }
        legacyTlsFactory = new LegacyTlsSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
    }

    public Document fetchCalendar(int year) {
        log.info("세종대 포털 SSO 로그인 시도");
        Map<String, String> cookies = login();

        log.info("학사일정 페이지 요청: year={}", year);
        try {
            return Jsoup.connect(config.getCalendarUrl())
                    .sslSocketFactory(legacyTlsFactory)
                    .userAgent(USER_AGENT)
                    .cookies(cookies)
                    .data("yy", String.valueOf(year))
                    .data("searchGubun", "0001")
                    .data("currentPageNo", "1")
                    .data("dataSeq", "")
                    .data("searchValue", "")
                    .timeout(15_000)
                    .get();
        } catch (IOException e) {
            throw new DoogooException(ErrorCode.ACADEMIC_CRAWL_FAILED, e);
        }
    }

    private Map<String, String> login() {
        try {
            Map<String, String> cookies = new HashMap<>();

            Connection.Response step1 = Jsoup.connect(config.getLoginUrl())
                    .sslSocketFactory(legacyTlsFactory)
                    .userAgent(USER_AGENT)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .execute();
            cookies.putAll(step1.cookies());

            Connection.Response step2 = Jsoup.connect(PORTAL_BASE + "/jsp/login/login_action.jsp")
                    .sslSocketFactory(legacyTlsFactory)
                    .userAgent(USER_AGENT)
                    .header("Referer", config.getLoginUrl())
                    .header("Origin", PORTAL_BASE)
                    .cookies(cookies)
                    .data("id", config.getId())
                    .data("password", config.getPassword())
                    .data("mainLogin", "Y")
                    .data("rtUrl", "")
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.POST)
                    .followRedirects(false)
                    .execute();
            cookies.putAll(step2.cookies());
            if (!cookies.containsKey("ssotoken")) {
                log.error("[{}] {}: ssotoken 쿠키 없음, loginUrl={}", ErrorCode.ACADEMIC_LOGIN_FAILED.getCode(), ErrorCode.ACADEMIC_LOGIN_FAILED.getMessage(), config.getLoginUrl());
                throw new DoogooException(ErrorCode.ACADEMIC_LOGIN_FAILED);
            }

            Connection.Response step3 = Jsoup.connect(PORTAL_BASE + "/comm/member/user/ssoLoginProc.do")
                    .sslSocketFactory(legacyTlsFactory)
                    .userAgent(USER_AGENT)
                    .cookies(cookies)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .followRedirects(true)
                    .execute();
            cookies.putAll(step3.cookies());

            if (!cookies.containsKey("PO_JSESSIONID")) {
                log.error("[{}] {}: PO_JSESSIONID 쿠키 없음", ErrorCode.ACADEMIC_LOGIN_FAILED.getCode(), ErrorCode.ACADEMIC_LOGIN_FAILED.getMessage());
                throw new DoogooException(ErrorCode.ACADEMIC_LOGIN_FAILED);
            }

            log.info("[로그인] 성공 - 쿠키: {}", cookies.keySet());
            return cookies;

        } catch (IOException e) {
            log.error("[{}] {}: {}", ErrorCode.ACADEMIC_CRAWL_FAILED.getCode(), ErrorCode.ACADEMIC_CRAWL_FAILED.getMessage(), e.getMessage(), e);
            throw new DoogooException(ErrorCode.ACADEMIC_CRAWL_FAILED, e);
        }
    }
}
