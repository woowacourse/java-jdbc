package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        Connection connection = getConnection();

        jdbcTemplate.executeQuery(
                connection,
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }

    private Connection getConnection() {
        DataSource dataSource = DataSourceConfig.getInstance();
        return DataSourceUtils.getConnection(dataSource);
    }
}
