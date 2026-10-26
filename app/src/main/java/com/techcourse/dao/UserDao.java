package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail()
        );
    }

    public void update(final User user) {
        try (Connection connection = dataSource.getConnection()) {
            update(connection, user);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public void update(final Connection connection, final User user) {
        jdbcTemplate.update(
                connection,
                "update users set account = ?, password = ?, email = ? where id = ?",
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId()
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.selectMany(
                "select id, account, password, email from users",
                this::parseUser
        );
    }

    public User findById(final Long id) {
        try (Connection connection = dataSource.getConnection()) {
            return findById(connection, id);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public User findById(final Connection connection, final Long id) {
        return jdbcTemplate.selectOne(
                connection,
                "select id, account, password, email from users where id = ?",
                this::parseUser,
                id
        ).orElse(null);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.selectOne(
                "select id, account, password, email from users where account = ?",
                this::parseUser,
                account
        ).orElse(null);
    }

    private User parseUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
        );
    }
}
