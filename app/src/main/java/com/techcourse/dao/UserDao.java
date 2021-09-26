package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = rs -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
    );

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());

    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(sql, rowMapper);
    }

    public Optional<User> findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return Optional.ofNullable(jdbcTemplate.query(sql, rowMapper, pstmt -> pstmt.setLong(1, id)));
    }

    public Optional<User> findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        return Optional.ofNullable(jdbcTemplate.query(sql, rowMapper, pstmt -> pstmt.setString(1, account)));
    }
}
