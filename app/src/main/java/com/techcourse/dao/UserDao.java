package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
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

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        jdbcTemplate.update(
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        jdbcTemplate.update(
                sql,
                user.getPassword(),
                user.getId()
        );
    }


    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        return jdbcTemplate.query(sql, userRowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        List<User> result = jdbcTemplate.query(sql, userRowMapper, id);

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        List<User> result = jdbcTemplate.query(sql, userRowMapper, account);

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
