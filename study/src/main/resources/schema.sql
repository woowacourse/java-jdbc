CREATE TABLE IF NOT EXISTS users(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    account  VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email    VARCHAR(100) NOT NULL
) ENGINE = INNODB;
