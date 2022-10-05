package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.UserHistory;

import nextstep.jdbc.JdbcTemplate;

public class JdbcUserHistoryDao implements UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcUserHistoryDao.class);

    private final DataSource dataSource;

    public JdbcUserHistoryDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JdbcUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    @Override
    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);

            log.debug("query : {}", sql);

            statement.setLong(1, userHistory.getUserId());
            statement.setString(2, userHistory.getAccount());
            statement.setString(3, userHistory.getPassword());
            statement.setString(4, userHistory.getEmail());
            statement.setObject(5, userHistory.getCreatedAt());
            statement.setString(6, userHistory.getCreateBy());
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
}
