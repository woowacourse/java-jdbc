package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(InsertJdbcTemplate.class);

    private final UserDao userDao;

    public InsertJdbcTemplate(final UserDao userDao) {
        this.userDao = userDao;
    }

    public int insert(final User user) {
        final var sql = createQuery();
        log.debug("query : {}", sql);
        try (final var connection = userDao.getDataSource().getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setValues(user, statement);
            return statement.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private String createQuery() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    private void setValues(final User user, final PreparedStatement statement) throws SQLException {
        statement.setString(1, user.getAccount());
        statement.setString(2, user.getPassword());
        statement.setString(3, user.getEmail());
    }
}
