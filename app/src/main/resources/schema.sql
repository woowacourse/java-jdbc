DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT AUTO_INCREMENT,
    account  VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email    VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS user_history;
CREATE TABLE IF NOT EXISTS user_history
(
    id         BIGINT AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    account    VARCHAR(100) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    created_at DATETIME     NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);
