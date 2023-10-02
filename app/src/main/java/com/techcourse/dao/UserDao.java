package com.techcourse.dao;

import com.techcourse.dao.exception.UserNotExistException;
import com.techcourse.domain.User;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    // TODO: 2023-10-02 count 변수 사용처 확인 -> 한 번 출력해보기
    private static final RowMapper<User> ROW_MAPPER = (rs, count) ->
        new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
        );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, email, password from users";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotExistException();
        }
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        try {
            return jdbcTemplate.queryForObject(sql, ROW_MAPPER, account);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotExistException();
        }
    }
}
