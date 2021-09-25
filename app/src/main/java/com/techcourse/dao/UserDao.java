package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_MAPPER = (resultSet, rowNum) -> new User(
        resultSet.getLong(1),
        resultSet.getString(2),
        resultSet.getString(3),
        resultSet.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        return jdbcTemplate.queryAsList(sql, USER_MAPPER);
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.query(sql, USER_MAPPER, id);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.query(sql, USER_MAPPER, account);
    }
}
