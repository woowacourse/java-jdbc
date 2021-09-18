package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private static UserDao userDao;

    private final JdbcTemplate jdbcTemplate;

    private UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };
    }

    public static UserDao getInstance() {
        if (Objects.isNull(userDao)) {
            userDao = new UserDao(DataSourceConfig.getInstance());
        }
        return userDao;
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set password = ? where id = ?";
        jdbcTemplate.update(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, id));
    }

    public Optional<User> findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, account));
    }
}
