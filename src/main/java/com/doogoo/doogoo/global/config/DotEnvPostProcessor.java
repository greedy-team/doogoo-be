package com.doogoo.doogoo.global.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 프로젝트 루트의 .env 파일을 Spring 환경에 자동으로 로드합니다.
 * 이미 OS 환경변수로 설정된 값은 덮어쓰지 않습니다.
 */
public class DotEnvPostProcessor implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String ENV_FILE = ".env";
    private static final String PROPERTY_SOURCE_NAME = "dotenv";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Path envFile = findEnvFile();
        if (envFile == null) return;

        ConfigurableEnvironment environment = event.getEnvironment();
        Map<String, Object> properties = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(envFile)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";")) continue;

                int idx = trimmed.indexOf('=');
                if (idx < 1) continue;

                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();

                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                if (System.getenv(key) != null) continue;

                properties.put(key, value);
            }
        } catch (IOException e) {
            return;
        }

        if (!properties.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private Path findEnvFile() {
        Path current = Paths.get(System.getProperty("user.dir"), ENV_FILE);
        if (Files.exists(current)) return current;
        return null;
    }
}
