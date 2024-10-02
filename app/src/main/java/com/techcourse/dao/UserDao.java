package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet, rowNum) -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, new Object[]{user.getAccount(), user.getPassword(), user.getEmail()});
    }

    public void update(final User user) {
        String sql = "update users set account = ?, email = ?, password = ?";
        jdbcTemplate.update(sql, new Object[]{user.getAccount(), user.getEmail(), user.getPassword()});
    }

    public List<User> findAll() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, USER_ROW_MAPPER)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
    }

    public User findByAccount(final String account) {
        String sql = "SELECT * FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(sql, new String[]{account}, USER_ROW_MAPPER)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
    }
}
