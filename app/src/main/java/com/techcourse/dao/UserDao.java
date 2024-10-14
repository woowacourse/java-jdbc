package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(User user) {
        String sql = "update users set account=?, password=?, email=? where id=?";

        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        String sql = "select * from users";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(Long id) {
        String sql = "select * from users where id = ?";

        return jdbcTemplate.queryForObject(sql, pstmt -> pstmt.setLong(1, id), USER_ROW_MAPPER);
    }

    public User findByAccount(String account) {
        String sql = "select * from users where account = ?";

        return jdbcTemplate.queryForObject(sql, pstmt -> pstmt.setString(1, account), USER_ROW_MAPPER);
    }
}
