package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private final RowMapper<User> rowMapper = rs -> new User(
        rs.getLong(1),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4)
    );

    public void insert(User user) {
        jdbcTemplate.update(
            "insert into users (account, password, email) values (?, ?, ?)",
            user.getAccount(),
            user.getPassword(),
            user.getEmail()
        );
    }

    public void update(User user) {
        jdbcTemplate.update(
            "update users set account = ?, password = ?, email = ?  where id = ?",
            user.getAccount(),
            user.getPassword(),
            user.getEmail(),
            user.getId()
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.queryForList(
            "select id, account, password, email from users",
            pstmt -> {
            },
            rowMapper
        );
    }

    public User findById(Long id) {
        return jdbcTemplate.queryForObject(
            "select id, account, password, email from users where id = ?",
            pstmt -> pstmt.setLong(1, id),
            rowMapper
        );
    }

    public User findByAccount(String account) {
        return jdbcTemplate.queryForObject(
            "select id, account, password, email from users where account = ?",
            pstmt -> pstmt.setString(1, account),
            rowMapper
        );
    }

    public void deleteAll() {
        jdbcTemplate.update(
            "delete users",
            pstmt -> {
            }
        );
    }
}
