CREATE TABLE IF NOT EXISTS roles (
                                     id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name       VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS users (
                                     id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name         VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    enabled      BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- removed: ON UPDATE CURRENT_TIMESTAMP (MySQL only)
    );

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
                                          PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              token       VARCHAR(512) NOT NULL UNIQUE,
    user_id     BIGINT NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- H2 + MySQL compatible (no INSERT IGNORE)
MERGE INTO roles (name) KEY(name)
    VALUES ('ROLE_USER'), ('ROLE_ADMIN');