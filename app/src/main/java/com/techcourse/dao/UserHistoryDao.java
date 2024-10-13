package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementArgumentsSetter;
import com.interface21.jdbc.core.QueryConnectionHolder;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);
    private static final RowMapper<UserHistory> ROW_MAPPER = rs -> new UserHistory(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getString("created_by")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserHistoryDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void log(UserHistory userHistory) {
        String sql = "INSERT INTO user_history (user_id, account, password, email, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        logSql(sql);

        jdbcTemplate.update(
                sql,
                userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy()
        );
    }

    public void logUsingExplicitConnection(UserHistory userHistory, Connection connection) {
        String sql = "INSERT INTO user_history (user_id, account, password, email, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        logSql(sql);
        QueryConnectionHolder queryConnectionHolder = new QueryConnectionHolder(connection, sql);
        PreparedStatementArgumentsSetter argumentsSetter = new PreparedStatementArgumentsSetter(
                userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy()
        );
        jdbcTemplate.update(queryConnectionHolder, argumentsSetter);
    }

    public UserHistory findById(Long id) {
        String sql = "SELECT id, user_id, account, password, email, created_at, created_by FROM user_history WHERE id = ?";
        logSql(sql);
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    private void logSql(String sql) {
        log.debug("query : {}", sql);
    }
}
