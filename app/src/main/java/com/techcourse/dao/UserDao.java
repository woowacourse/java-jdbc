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
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, new InsertUserPreparedStatementSetter(user));
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, new UpdateUserPreparedStatementSetter(user));
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return (User) jdbcTemplate.query(sql, new SelectUserPreparedStatementSetter(id), new UserRowMapper());
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return (User) jdbcTemplate.query(sql, pstmt -> pstmt.setString(1, account), new UserRowMapper());
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
