DROP TABLE users IF EXISTS;
DROP TABLE user_history IF EXISTS;

CREATE TABLE IF NOT EXISTS users (
    id bigint auto_increment,
    account VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS user_history (
    id bigint auto_increment,
    user_id bigint not null,
    account VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at datetime not null,
    created_by VARCHAR(100) NOT NULL,
    PRIMARY KEY(id)
);
