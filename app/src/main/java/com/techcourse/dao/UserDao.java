package com.techcourse.dao;

import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper userRowMapper = new RowMapper() {
        @Override
        public Object mapRow(final ResultSet resultSet) throws SQLException {
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );
        }
    };

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final PreparedStatementSetter setter = new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getAccount());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getEmail());
            }
        };

        jdbcTemplate.update(sql, setter);
    }

    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";
        final PreparedStatementSetter setter = new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getPassword());
                statement.setLong(2, user.getId());
            }
        };

        jdbcTemplate.update(sql, setter);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return (List<User>) jdbcTemplate.query(sql, null, userRowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final PreparedStatementSetter setter = new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement statement) throws SQLException {
                statement.setObject(1, id);
            }
        };

        final List<?> users = (List<?>) jdbcTemplate.query(sql, setter, userRowMapper);
        return users.isEmpty() ? null : (User) users.getFirst();
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final PreparedStatementSetter setter = new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, account);
            }
        };

        final List<?> users = (List<?>) jdbcTemplate.query(sql, setter, userRowMapper);
        return users.isEmpty() ? null : (User) users.getFirst();
    }
}
