package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao() {
        this(DataSourceConfig.getInstance());
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.queryMany(sql, USER_ROW_MAPPER);
    }

    public Optional<User> findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return findUser(sql, id);
    }

    public Optional<User> findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return findUser(sql, account);
    }

    private Optional<User> findUser(String sql, Object... args) {
        try {
            User user = jdbcTemplate.queryOne(sql, USER_ROW_MAPPER, args);
            return Optional.of(user);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }
}
