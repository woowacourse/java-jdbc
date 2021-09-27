package com.techcourse.dao;

import java.util.List;
import javax.sql.DataSource;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.PreparedStatementSetter;
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

        PreparedStatementSetter preparedStatementSetter = preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
        };

        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.executeQuery(sql, preparedStatementSetter);
    }

    public void update(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        PreparedStatementSetter preparedStatementSetter = preparedStatement -> {
            preparedStatement.setString(1, user.getPassword());
            preparedStatement.setLong(2, user.getId());
        };

        String sql = "update users set password = ? where id = ?";
        jdbcTemplate.executeQuery(sql, preparedStatementSetter);
    }

    public List<User> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        RowMapper rowMapper = resultSet -> new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
        );

        String sql = "select id, account, password, email from users";
        return (List<User>) jdbcTemplate.query(sql, null, rowMapper);
    }

    public User findById(Long id) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        PreparedStatementSetter preparedStatementSetter =
                preparedStatement -> preparedStatement.setLong(1, id);

        RowMapper<User> rowMapper = resultSet -> new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
        );

        String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, preparedStatementSetter, rowMapper);
    }

    public User findByAccount(String account) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        PreparedStatementSetter preparedStatementSetter =
                preparedStatement -> preparedStatement.setString(1, account);

        RowMapper<User> rowMapper = resultSet -> new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
        );

        String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, preparedStatementSetter, rowMapper);
    }
}
