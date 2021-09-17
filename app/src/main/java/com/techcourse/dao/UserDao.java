package com.techcourse.dao;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<User> findAll() {
        final JdbcTemplate<List<User>> jdbcTemplate = new JdbcTemplate<>() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "select id, account, password, email from users";
        final RowMapper<List<User>> rowMapper = rs -> {
            final List<User> users = new ArrayList<>();
            while (rs.next()) {
                final User user = getUserSetWithResultValues(rs);
                users.add(user);
            }
            return users;
        };

        return jdbcTemplate.query(sql, rowMapper);
    }

    private User getUserSetWithResultValues(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4));
    }

    private RowMapper<User> getRowMapperForOnlyOneUserResult() {
        return this::getUserMappedByResultSet;
    }

    private User getUserMappedByResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return getUserSetWithResultValues(rs);
        }
        return null;
    }

    public User findByAccount(String account) {
        final JdbcTemplate<User> jdbcTemplate = new JdbcTemplate<>() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "select id, account, password, email from users where account = ?";
        final RowMapper<User> rowMapper = getRowMapperForOnlyOneUserResult();

        return jdbcTemplate.query(sql, rowMapper, account);
    }

    public User findById(Long id) {
        final JdbcTemplate<User> jdbcTemplate = new JdbcTemplate<>() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "select id, account, password, email from users where id = ?";
        final RowMapper<User> rowMapper = getRowMapperForOnlyOneUserResult();

        return jdbcTemplate.query(sql, rowMapper, id);
    }

    public void insert(User user) {
        final JdbcTemplate<User> jdbcTemplate = new JdbcTemplate<>() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql,
            user.getAccount(),
            user.getPassword(),
            user.getEmail()
        );
    }

    public void update(User user) {
        final JdbcTemplate<Void> jdbcTemplate = new JdbcTemplate<>() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql,
            user.getAccount(),
            user.getPassword(),
            user.getEmail(),
            user.getId()
        );
    }
}
