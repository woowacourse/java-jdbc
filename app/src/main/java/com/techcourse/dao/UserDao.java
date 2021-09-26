package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, new InsertUserPreparedStatementSetter(user));
    }

    public void update(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, new UpdateUserPreparedStatementSetter(user));
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(Long id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        String sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.query(sql, new SelectUserPreparedStatementSetter(id), new UserRowMapper());
    }

    public User findByAccount(String account) {
        // todo
        return null;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
