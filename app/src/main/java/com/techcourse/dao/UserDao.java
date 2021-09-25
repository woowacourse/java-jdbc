package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.mapper.ResultSetToObjectMapper;

public class UserDao {

    private static final ResultSetToObjectMapper<User> USER_MAPPER = rs ->
        new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
        );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.executeInsertOrUpdateOrDelete(sql,
            user.getAccount(),
            user.getPassword(),
            user.getEmail()
        );
    }

    public void update(User user) {
        final String sql = "update users set account=?, password=?, email=? where id = ?";

        jdbcTemplate.executeInsertOrUpdateOrDelete(sql,
            user.getAccount(),
            user.getPassword(),
            user.getEmail(),
            user.getId()
        );
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        return jdbcTemplate.queryForMany(sql, USER_MAPPER);
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql,
            USER_MAPPER,
            id
        );
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql,
            USER_MAPPER,
            account
        );
    }
}
