package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import nextstep.web.annotation.Repository;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = resultSet -> {
            if (resultSet.next()) {
                return new User(
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4));
            }
            return null;
        };
    }

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users SET account = ?, password = ?, email = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.queryObjects(sql, rowMapper);
    }

    public Optional<User> findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryObjectWithCondition(sql, rowMapper, id);
    }

    public Optional<User> findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryObjectWithCondition(sql, rowMapper, account);
    }

    public Optional<User> findByAccountAndPassoword(String account, String password) {
        final String sql = "select id, account, password, email from users where account = ? and password = ?";
        return jdbcTemplate.queryObjectWithCondition(sql, rowMapper, account, password);
    }
}
