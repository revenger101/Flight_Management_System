CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(20) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(190),
    enabled BIT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_app_user_email UNIQUE (email)
);
