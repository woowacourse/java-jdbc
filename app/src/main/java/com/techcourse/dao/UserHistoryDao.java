package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.querybuilder.QueryBuilder;
import com.interface21.jdbc.querybuilder.query.Query;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(Connection connection, UserHistory userHistory) {
        Query query = new QueryBuilder()
                .insert("user_id", "account", "password", "email", "created_at", "created_by")
                .into("user_history")
                .build();

        jdbcTemplate.queryForUpdate(
                connection,
                query.getSql(),
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
