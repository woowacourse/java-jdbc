package com.techcourse.dao;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ObjectMapper;
import com.interface21.jdbc.core.OrderedSetter;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);
    private static final ObjectMapper<UserHistory> HISTORY_OBJECT_MAPPER = (resultSet, rowNum) -> new UserHistory(
            resultSet.getLong("id"),
            resultSet.getLong("user_id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"),
            resultSet.getString("created_by"));
    private static final PreparedStatementSetter ORDERED_SETTER = new OrderedSetter();

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserHistory findById(Connection connection, Long id) {
        return jdbcTemplate.getResult(connection, ORDERED_SETTER, "select * from user_history where id = ?",
                HISTORY_OBJECT_MAPPER, id);
    }

    public int log(Connection connection, UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values "
                + "(?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.execute(connection, ORDERED_SETTER,
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy());
    }
}
