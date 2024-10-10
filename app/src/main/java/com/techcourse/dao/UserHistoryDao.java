package com.techcourse.dao;

import com.interface21.jdbc.core.ArgumentsPreparedStatementSetter;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementCreator;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserHistoryDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void log(Connection connection, UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator(connection, sql);
        jdbcTemplate.update(
                preparedStatementCreator,
                new ArgumentsPreparedStatementSetter(
                        userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                        userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreatedBy()
                )
        );
    }

    public void log(UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreatedBy()
        );
    }
}
