package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private final DataSource dataSource;
    private final RowMapper<User> userRowMapper = (resultSet) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        PreparedStatementSetter setter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        jdbcTemplate.update(sql, setter);
    }

    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";

        PreparedStatementSetter setter = pstmt -> {
            pstmt.setString(1, user.getPassword());
            pstmt.setLong(2, user.getId());
        };

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        jdbcTemplate.update(sql, setter);
    }


    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        PreparedStatementSetter setter = pstmt -> {};

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        return jdbcTemplate.query(sql, setter, userRowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        PreparedStatementSetter setter = pstmt -> {
            pstmt.setLong(1, id);
        };

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        List<User> result = jdbcTemplate.query(sql, setter, userRowMapper);

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        PreparedStatementSetter setter = pstmt -> {
            pstmt.setString(1, account);
        };

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        List<User> result = jdbcTemplate.query(sql, setter, userRowMapper);

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
