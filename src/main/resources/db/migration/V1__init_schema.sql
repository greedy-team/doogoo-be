-- subscriptions (ICS 구독)
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    source_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    alarm_enabled BOOLEAN NOT NULL,
    alarm_minutes_before INT,
    created_at TIMESTAMP NOT NULL,
    last_accessed_at TIMESTAMP,
    enabled BOOLEAN NOT NULL,
    CONSTRAINT uk_subscription_token UNIQUE (token)
);

-- academic_notices (학사 공지)
CREATE TABLE academic_notices (
    notice_id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    grade VARCHAR(255) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP
);
CREATE INDEX idx_academic_start ON academic_notices (start_at);
CREATE INDEX idx_academic_grade ON academic_notices (grade);

-- dodream_notices (두드림 공지)
CREATE TABLE dodream_notices (
    notice_id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    department_name VARCHAR(255),
    application_start_at TIMESTAMP,
    application_end_at TIMESTAMP,
    operating_start_at TIMESTAMP NOT NULL,
    operating_end_at TIMESTAMP,
    detail_url VARCHAR(255) NOT NULL
);

-- dodream_notice_keywords (@ElementCollection)
CREATE TABLE dodream_notice_keywords (
    dodream_notice_notice_id VARCHAR(255) NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    PRIMARY KEY (dodream_notice_notice_id, keyword),
    CONSTRAINT fk_dodream_notice_keywords_notice
        FOREIGN KEY (dodream_notice_notice_id) REFERENCES dodream_notices (notice_id)
);
