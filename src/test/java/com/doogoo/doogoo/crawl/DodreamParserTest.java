package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.dodream.domain.EventDto;
import com.doogoo.doogoo.global.config.CrawlConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DodreamParserTest {

    private DodreamParser dodreamParser;
    private CrawlConfig crawlConfig;

    @BeforeEach
    void setUp() {
        crawlConfig = new CrawlConfig();
        crawlConfig.setBaseUrl("https://do.sejong.ac.kr");
        crawlConfig.setDetailPath("/ko/program/all/view/{id}");
        dodreamParser = new DodreamParser(crawlConfig);
    }

    @Test
    @DisplayName("목록 페이지 HTML(dodream.main.html)에서 데이터가 정상적으로 파싱되어야 한다")
    void parseList_success() throws Exception {
        File htmlFile = new ClassPathResource("dodream.main.html").getFile();
        Document doc = Jsoup.parse(htmlFile, StandardCharsets.UTF_8.name());

        List<EventDto> events = dodreamParser.parseList(doc);

        assertThat(events).isNotNull();
        System.out.println("✅ 파싱된 목록 개수: " + events.size() + "개");

        if (!events.isEmpty()) {
            for (int i = 0; i < Math.min(3, events.size()); i++) {
                EventDto event = events.get(i);
                System.out.println("--- [ " + (i + 1) + "번째 공지 ] ---");
                System.out.println("아이디: " + event.dodreamId());
                System.out.println("제목: " + event.title());
                System.out.println("부서: " + event.department());
                System.out.println("신청기간: " + event.applyStart() + " ~ " + event.applyEnd());
                System.out.println("운영기간: " + event.operateStart() + " ~ " + event.operateEnd());
                System.out.println("링크: " + event.dodreamUrl());
            }
        }

        assertThat(events).isNotEmpty();
    }

    @Test
    @DisplayName("상세 페이지 HTML(dodream.detail.html)에서 장소와 상세내용이 파싱되어야 한다")
    void parseDetail_success() throws Exception {
        File htmlFile = new ClassPathResource("dodream.detail.html").getFile();
        Document doc = Jsoup.parse(htmlFile, StandardCharsets.UTF_8.name());

        EventDto dummyListData = EventDto.builder()
                                         .dodreamId(12345L)
                                         .title("테스트 상세 공지")
                                         .build();

        EventDto detailedEvent = dodreamParser.parseDetail(doc, dummyListData);

        assertThat(detailedEvent).isNotNull();

        String desc = detailedEvent.description();
        String safeDesc = (desc != null && desc.length() > 50) ? desc.substring(0, 50) + "..." : desc;

        System.out.println("✅ 상세 내용(앞부분): " + safeDesc);
        System.out.println("✅ 장소: " + detailedEvent.location());
        System.out.println("✅ 운영시작 갱신: " + detailedEvent.operateStart());
        System.out.println("✅ 운영종료 갱신: " + detailedEvent.operateEnd());
    }

    @Test
    @DisplayName("마감된 페이지 HTML(dodream.closed.html)에서 마감된 공지 ID들만 정상적으로 추출되어야 한다")
    void parseClosedIds_success() throws Exception {
        File htmlFile = new ClassPathResource("dodream.closed.html").getFile();
        Document doc = Jsoup.parse(htmlFile, StandardCharsets.UTF_8.name());

        List<Long> closedIds = dodreamParser.parseClosedIds(doc);

        assertThat(closedIds).isNotNull();
        System.out.println("✅ 파싱된 마감 공지 개수: " + closedIds.size() + "개");

        if (!closedIds.isEmpty()) {
            System.out.println("✅ 마감 처리될 공지 ID 목록: " + closedIds);
        }

        assertThat(closedIds).isNotEmpty();
    }
}
