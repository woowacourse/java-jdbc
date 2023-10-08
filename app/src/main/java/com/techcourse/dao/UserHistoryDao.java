package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);
    private static final RowMapper<UserHistory> userHistoryRowMapper = (resultSet) -> new UserHistory(
            resultSet.getLong("id"),
            resultSet.getLong("user_id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"),
            resultSet.getString("created_by")
    );

    private static final String INSERT_USER_HISTORY_SQL = "insert into user_history "
            + "(user_id, account, password, email, created_at, created_by) "
            + "values (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        log.debug("User history id : {}", userHistory.getUserId());
        jdbcTemplate.update(
                INSERT_USER_HISTORY_SQL,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        log.debug("User history id : {}", userHistory.getUserId());
        jdbcTemplate.update(
                connection,
                INSERT_USER_HISTORY_SQL,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }

    public UserHistory findLogByUser(final User user) {
        log.debug("User history id : {}", user.getId());
        final var sql = "select * from user_history where user_id = ?";
        return jdbcTemplate.queryForObject(sql, userHistoryRowMapper, user.getId());
    }
}
