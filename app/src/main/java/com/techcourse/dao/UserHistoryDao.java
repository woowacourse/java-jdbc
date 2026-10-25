package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;

import javax.sql.DataSource;
import java.sql.Connection;

public class UserHistoryDao {

    private static final String INSERT_SQL = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        jdbcTemplate.command(INSERT_SQL, historySetter(userHistory));
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        jdbcTemplate.command(connection, INSERT_SQL, historySetter(userHistory));
    }

    private PreparedStatementSetter historySetter(final UserHistory userHistory) {
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
