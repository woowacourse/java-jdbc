-- mysql 8.0.30부터는 statement.execute()으로 여러 쿼리를 한 번에 실행할 수 없다.
-- 멀티 쿼리 옵션을 url로 전달하도록 수정하는 방법을 찾아서 적용하자.
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS user_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL
) ENGINE=INNODB;
