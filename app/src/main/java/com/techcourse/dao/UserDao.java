package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private static final String UPDATE_SQL = "update users set account=?, password=?, email=? where users.id=?";
    private static final String FIND_BY_ID_SQL = "select id, account, password, email from users where id = ?";

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        jdbcTemplate.update(UPDATE_SQL, updateParameters(user));
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users;";
        return jdbcTemplate.query(sql, userRowMapper());
    }

    public User findById(final Long id) {
        return requireUser(jdbcTemplate.query(FIND_BY_ID_SQL, userRowMapper(), id));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return requireUser(jdbcTemplate.query(sql, userRowMapper(), account));
    }

    private User requireUser(final List<User> users) {
        if (users.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        return users.getFirst();
    }

    private Object[] updateParameters(final User user) {
        return new Object[]{user.getAccount(), user.getPassword(), user.getEmail(), user.getId()};
    }

    private RowMapper<User> userRowMapper() {
        return resultSet -> {
            long id = resultSet.getLong("id");
            String account = resultSet.getString("account");
            String password = resultSet.getString("password");
            String email = resultSet.getString("email");
            return new User(id, account, password, email);
        };
    }
}
