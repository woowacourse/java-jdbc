package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserHistoryDao {
    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        String sql = insertSql();
        PreparedStatementSetter preparedStatementSetter = createInsertpreparedStatementSetter(userHistory);
        jdbcTemplate.update(sql, preparedStatementSetter);
    }

    public void log(UserHistory userHistory, Connection connection) {
        String sql = insertSql();
        PreparedStatementSetter preparedStatementSetter = createInsertpreparedStatementSetter(userHistory);
        jdbcTemplate.update(sql, preparedStatementSetter, connection);
    }

    private String insertSql() {
        return "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
    }

    private PreparedStatementSetter createInsertpreparedStatementSetter(UserHistory userHistory) {
        return pstmt -> {
            pstmt.setLong(1, userHistory.getUserId());
            pstmt.setString(2, userHistory.getAccount());
            pstmt.setString(3, userHistory.getPassword());
            pstmt.setString(4, userHistory.getEmail());
            pstmt.setObject(5, userHistory.getCreatedAt());
            pstmt.setString(6, userHistory.getCreateBy());
        };
    }
}
