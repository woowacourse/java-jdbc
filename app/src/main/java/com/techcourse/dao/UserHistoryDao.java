package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.Optional;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<UserHistory> userHistoryRowMapper() {
        return rs -> new UserHistory(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("created_by"));
    }

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy());
    }

    public Optional<UserHistory> findLogByUser(final User user) {
        final var sql = "select id, user_id, account, password, email, created_by from user_history where user_id = ?";
        return jdbcTemplate.queryForObject(sql, userHistoryRowMapper(), user.getId());
    }
}
