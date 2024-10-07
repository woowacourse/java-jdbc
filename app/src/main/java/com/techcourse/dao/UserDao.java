package com.techcourse.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var query = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(query, pstmt -> {
            pstmt.setObject(1, user.getAccount());
            pstmt.setObject(2, user.getPassword());
            pstmt.setObject(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var query = "update users set account = ?, password = ?, email = ?";

        jdbcTemplate.update(query, pstmt -> {
            pstmt.setObject(1, user.getAccount());
            pstmt.setObject(2, user.getPassword());
            pstmt.setObject(3, user.getEmail());
        });
    }

    public List<User> findAll() {
        final var query = "select id, account, password, email from users";

        return jdbcTemplate.query(query, this::mapUserFromResultSet, pstmt -> {
        });
    }

    public User findById(final Long id) {
        final var query = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(query, this::mapUserFromResultSet, pstmt -> pstmt.setObject(1, id))
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 갖는 유저가 없습니다."));
    }

    public User findByAccount(final String account) {
        final var query = "select * from users where account = ?";

        return jdbcTemplate.queryForObject(query, this::mapUserFromResultSet, pstmt -> pstmt.setObject(1, account))
                .orElseThrow(() -> new IllegalArgumentException("해당하는 account를 갖는 유저가 없습니다."));
    }

    private User mapUserFromResultSet(final ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        );
    }
}
