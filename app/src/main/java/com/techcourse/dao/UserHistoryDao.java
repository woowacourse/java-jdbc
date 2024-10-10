package com.techcourse.dao;

import com.interface21.jdbc.core.ArgumentPreparedStatementSetter;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final UserHistory userHistory) {
        String sql = """
                INSERT INTO user_history (user_id, account, password, email, created_at, created_by)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter =
                new ArgumentPreparedStatementSetter(
                        userHistory.getUserId(),
                        userHistory.getAccount(),
                        userHistory.getPassword(),
                        userHistory.getEmail(),
                        userHistory.getCreatedAt(),
                        userHistory.getCreateBy()
                );
        jdbcTemplate.update(sql, argumentPreparedStatementSetter);
        log.info("userHistory 업데이트에 성공했습니다.");
    }
}
