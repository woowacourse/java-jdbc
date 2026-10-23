package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final DataSource dataSource;
    private final com.interface21.jdbc.core.JdbcTemplate queryJdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.queryJdbcTemplate = new com.interface21.jdbc.core.JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(final User user, final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getAccount());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getEmail());
            }
        };

        jdbcTemplate.update(user);
    }

    public void update(final User user) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set password = ? where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(final User user, final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getPassword());
                statement.setLong(2, user.getId());
            }
        };

        jdbcTemplate.update(user);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return queryJdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return queryJdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return queryJdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
