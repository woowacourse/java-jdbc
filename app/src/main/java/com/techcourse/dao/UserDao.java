package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.rowmapper.RowMapper;

public class UserDao {

    private static final RowMapper<User> rowMapper = resultSet ->
        new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
        );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<User> findAll() {

        final String sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, rowMapper);
    }

    public User findByAccount(String account) {

        final String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.query(sql, rowMapper, account);
    }

    public User findById(Long id) {

        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.query(sql, rowMapper, id);
    }

    public void insert(User user) {

        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {

        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }
}
