package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.config.SejongPortalConfig;
import com.doogoo.doogoo.exception.CustomException;
import com.doogoo.doogoo.exception.ErrorCode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AcademicCrawler {

    private static final Logger log = LoggerFactory.getLogger(AcademicCrawler.class);
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String PORTAL_BASE = "https://portal.sejong.ac.kr";

    private final SejongPortalConfig config;

    public AcademicCrawler(SejongPortalConfig config) {
        this.config = config;
    }

    public Document fetchCalendar(int year) {
        log.info("세종대 포털 SSO 로그인 시도");
        Map<String, String> cookies = login();

        log.info("학사일정 페이지 요청: year={}", year);
        try {
            return Jsoup.connect(config.getCalendarUrl())
                    .userAgent(USER_AGENT)
                    .cookies(cookies)
                    .data("year", String.valueOf(year))
                    .timeout(15_000)
                    .get();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.ACADEMIC_CRAWL_FAILED, e);
        }
    }

    /**
     * 세종대 포털 SSO 로그인 흐름:
     * 1. GET loginSSL.jsp → 초기 쿠키(PO1_JSESSIONID, WMONID 등)
     * 2. POST login_action.jsp (id/password/mainLogin=Y) → ssotoken 발급
     * 3. GET ssoLoginProc.do → PO_JSESSIONID 발급 (포털 인증 세션 완성)
     */
    private Map<String, String> login() {
        try {
            Map<String, String> cookies = new HashMap<>();

            // 1단계: 로그인 페이지 GET → 초기 쿠키 수집
            Connection.Response step1 = Jsoup.connect(config.getLoginUrl())
                    .userAgent(USER_AGENT)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .execute();
            cookies.putAll(step1.cookies());

            // 2단계: 아이디/비밀번호 POST → ssotoken 발급
            Connection.Response step2 = Jsoup.connect(PORTAL_BASE + "/jsp/login/login_action.jsp")
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
                throw new CustomException(ErrorCode.ACADEMIC_LOGIN_FAILED);
            }

            // 3단계: ssoLoginProc.do → PO_JSESSIONID 발급
            Connection.Response step3 = Jsoup.connect(PORTAL_BASE + "/comm/member/user/ssoLoginProc.do")
                    .userAgent(USER_AGENT)
                    .cookies(cookies)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .followRedirects(true)
                    .execute();
            cookies.putAll(step3.cookies());

            if (!cookies.containsKey("PO_JSESSIONID")) {
                throw new CustomException(ErrorCode.ACADEMIC_LOGIN_FAILED);
            }

            log.info("[로그인] 성공 - 쿠키: {}", cookies.keySet());
            return cookies;

        } catch (IOException e) {
            throw new CustomException(ErrorCode.ACADEMIC_CRAWL_FAILED, e);
        }
    }
}
