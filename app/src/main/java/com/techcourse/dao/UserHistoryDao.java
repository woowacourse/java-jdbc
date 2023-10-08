package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection,
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
