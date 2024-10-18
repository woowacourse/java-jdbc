package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                pss -> {
                    pss.setLong(1, userHistory.getUserId());
                    pss.setString(2, userHistory.getAccount());
                    pss.setString(3, userHistory.getPassword());
                    pss.setString(4, userHistory.getEmail());
                    pss.setObject(5, userHistory.getCreatedAt());
                    pss.setString(6, userHistory.getCreateBy());
                }
        );
    }
}
