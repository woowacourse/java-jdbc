package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
    );

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email =? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(sql, USER_ROW_MAPPER);
    }

    public User findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
