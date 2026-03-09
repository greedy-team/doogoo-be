package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class AcademicCrawler {

    private static final Logger log = LoggerFactory.getLogger(AcademicCrawler.class);

    public Document fetchCalendar(int year) {
        String filename = "academic-" + year + ".html";
        log.info("로컬 학사일정 파일 로드: {}", filename);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                log.error("학사일정 파일 없음: {}", filename);
                throw new DoogooException(ErrorCode.ACADEMIC_CRAWL_FAILED);
            }
            return Jsoup.parse(inputStream, "UTF-8", "");
        } catch (IOException e) {
            throw new DoogooException(ErrorCode.ACADEMIC_CRAWL_FAILED, e);
        }
    }
}
