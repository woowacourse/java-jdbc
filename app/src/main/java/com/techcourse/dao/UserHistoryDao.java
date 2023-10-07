package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

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
            userHistory.getCreatedBy()
        );
    }

    public UserHistory findById(Long id){
        String sql = "select * from user_history where id = ?";
        return jdbcTemplate.queryForObject(sql, UserHistory.class, id);
    }

    public void log(Connection conn, UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
            preparedStatement.setObject(1, userHistory.getUserId());
            preparedStatement.setObject(2, userHistory.getAccount());
            preparedStatement.setObject(3, userHistory.getPassword());
            preparedStatement.setObject(4, userHistory.getEmail());
            preparedStatement.setObject(5, userHistory.getCreatedAt());
            preparedStatement.setObject(6, userHistory.getCreatedBy());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
