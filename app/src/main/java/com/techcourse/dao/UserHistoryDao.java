package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.mapper.UserHistoryRowMapper;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;
    private final UserHistoryRowMapper userHistoryRowMapper;

    public UserHistoryDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userHistoryRowMapper = new UserHistoryRowMapper();
    }

    private void insert(final Connection connection, final UserHistory userHistory) {
        final String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
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

    public List<UserHistory> findAll() {
        final String sql = "select id, user_id, account, password, email, created_at, created_by from user_history";

        return jdbcTemplate.query(sql, userHistoryRowMapper);
    }

    public UserHistory findById(final Long userId) {
        final String sql = "select id, user_id, account, password, email, created_at, created_by from user_history where user_id = ?";

        return jdbcTemplate.queryForObject(sql, userHistoryRowMapper, userId);
    }

    public void log(Connection connection, UserHistory userHistory) {
        insert(connection, userHistory);
    }
}
