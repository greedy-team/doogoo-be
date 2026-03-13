package com.doogoo.doogoo.common.log;

import com.doogoo.doogoo.common.util.Sha256;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;


import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.entries;

public class JsonLog {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonLog() {
    }

    public static void info(Class<?> clazz, Object dto) {
        LoggerFactory.getLogger(clazz).info("event", entries(process(dto)));
    }

    public static void debug(Class<?> clazz, Object dto) {
        LoggerFactory.getLogger(clazz).debug("event", entries(process(dto)));
    }

    public static void warn(Class<?> clazz, Object dto) {
        LoggerFactory.getLogger(clazz).warn("event", entries(process(dto)));
    }

    public static void warn(Class<?> clazz, Object dto, Throwable t) {
        LoggerFactory.getLogger(clazz).warn("event", entries(process(dto)), t);
    }

    public static void error(Class<?> clazz, Object dto) {
        LoggerFactory.getLogger(clazz).error("event", entries(process(dto)));
    }

    public static void error(Class<?> clazz, Object dto, Throwable t) {
        LoggerFactory.getLogger(clazz).error("event", entries(process(dto)), t);
    }

    private static Map<String, Object> process(Object dto) {
        if (dto == null) {
            return Map.of();
        }

        Map<String, Object> map = objectMapper.convertValue(dto, Map.class);

        if (map.remove("token") instanceof String token) {
            map.put("tokenHash", Sha256.sha256(token).substring(0, 12));
        }

        return map;
    }
}
