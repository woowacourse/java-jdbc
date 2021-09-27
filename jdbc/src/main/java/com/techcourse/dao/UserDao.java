package com.techcourse.dao;

import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.executeQuery(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "update users set password = ? where id = ?";
        jdbcTemplate.executeQuery(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, (ResultSet resultSet) ->
                new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)
                ));
    }

    public User findById(Long id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, (ResultSet resultSet) ->
                new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)
                ), id);
    }

    public User findByAccount(String account) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, (ResultSet resultSet) ->
                new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)
                ), account);
    }
}
