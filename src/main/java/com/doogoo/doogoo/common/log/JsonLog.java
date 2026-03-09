package com.doogoo.doogoo.common.log;

import org.slf4j.LoggerFactory;

import static net.logstash.logback.argument.StructuredArguments.fields;

public class JsonLog {

    private JsonLog() {
    }


    public static void info(Class<?> clazz, Object dto) {
        LoggerFactory.getLogger(clazz).info("", fields(dto));
    }

    public static void debug(Class<?> clazz, Object dto) {
        LoggerFactory.getLogger(clazz).debug("", fields(dto));
    }

    public static void warn(Class<?> clazz, Object dto) {
        LoggerFactory.getLogger(clazz).warn("", fields(dto));
    }

    public static void warn(Class<?> clazz, Object dto, Throwable t) {
        LoggerFactory.getLogger(clazz).warn("", fields(dto), t);
    }

    public static void error(Class<?> clazz, Object dto) {
        LoggerFactory.getLogger(clazz).error("", fields(dto));
    }


    public static void error(Class<?> clazz, Object dto, Throwable t) {
        LoggerFactory.getLogger(clazz).error("", fields(dto), t);
    }
}
