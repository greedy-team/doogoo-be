package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.global.config.SejongPortalConfig;
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
        Map<String, String> cookies = login();

        try {
            return Jsoup.connect(config.getCalendarUrl())
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
                    .userAgent(USER_AGENT)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .execute();
            cookies.putAll(step1.cookies());

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
                throw new DoogooException(ErrorCode.ACADEMIC_LOGIN_FAILED);
            }

            Connection.Response step3 = Jsoup.connect(PORTAL_BASE + "/comm/member/user/ssoLoginProc.do")
                    .userAgent(USER_AGENT)
                    .cookies(cookies)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .followRedirects(true)
                    .execute();
            cookies.putAll(step3.cookies());

            if (!cookies.containsKey("PO_JSESSIONID")) {
                throw new DoogooException(ErrorCode.ACADEMIC_LOGIN_FAILED);
            }
            return cookies;

        } catch (IOException e) {
            throw new DoogooException(ErrorCode.ACADEMIC_CRAWL_FAILED, e);
        }
    }
}
