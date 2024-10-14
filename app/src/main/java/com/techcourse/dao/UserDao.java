package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.dao.rowmapper.UserRowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public UserDao(final JdbcTemplate jdbcTemplate, final UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.queryForList(sql, userRowMapper);
    }

    public Optional<User> findById(final Long id) {
        final String sql = "select * from users where id = ?";
        return jdbcTemplate.queryForOptional(sql, userRowMapper, id);
    }

    public Optional<User> findByAccount(final String account) {
        final String sql = "select * from users where account = ?";
        return jdbcTemplate.queryForOptional(sql, userRowMapper, account);
    }
}
