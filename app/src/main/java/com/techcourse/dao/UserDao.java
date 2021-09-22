package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
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
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";

        return jdbcTemplate.queryAsList(sql, USER_ROW_MAPPER);
    }

    public User findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER, account);
    }
}
