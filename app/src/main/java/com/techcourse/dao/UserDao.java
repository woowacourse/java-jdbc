package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.SQLException;
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

    public void insert(User user) throws SQLException {
        jdbcTemplate.update(
            "insert into users (account, password, email) values (?, ?, ?)",
            pstmt -> {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            });
    }

    public void update(User user) throws SQLException {
        jdbcTemplate.update(
            "update users set account = ?, password = ?, email = ?  where id = ?",
            pstmt -> {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            });
    }

    public List<User> findAll() throws SQLException {
        return jdbcTemplate.queryForList(
            "select id, account, password, email from users",
            pstmt -> {
            },
            rowMapper
        );
    }

    public User findById(Long id) throws SQLException {
        return jdbcTemplate.queryForObject(
            "select id, account, password, email from users where id = ?",
            pstmt -> pstmt.setLong(1, id),
            rowMapper
        );
    }

    public User findByAccount(String account) throws SQLException {
        return jdbcTemplate.queryForObject(
            "select id, account, password, email from users where account = ?",
            pstmt -> pstmt.setString(1, account),
            rowMapper
        );
    }

    public void deleteAll() throws SQLException {
        jdbcTemplate.update(
            "delete users",
            pstmt -> {
            }
        );
    }
}
