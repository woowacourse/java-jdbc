package com.techcourse.dao;

import com.interface21.jdbc.core.LegacyJdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class LegacyUserHistoryDaoImpl implements UserHistoryDao {

    private final LegacyJdbcTemplate legacyJdbcTemplate;

    public LegacyUserHistoryDaoImpl(final LegacyJdbcTemplate legacyJdbcTemplate) {
        this.legacyJdbcTemplate = legacyJdbcTemplate;
    }

    @Override
    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        legacyJdbcTemplate.executeUpdate(
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }

    @Override
    public void log(Connection conn, UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        legacyJdbcTemplate.executeUpdate(
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
