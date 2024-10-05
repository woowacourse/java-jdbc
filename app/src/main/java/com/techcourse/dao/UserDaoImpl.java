package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setObject(1, user.getAccount());
            pstmt.setObject(2, user.getPassword());
            pstmt.setObject(3, user.getEmail());
        };
        jdbcTemplate.update(sql, setter);
    }

    @Override
    public void update(User user) {
        String sql = "update users set password = ? where id = ?";
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setObject(1, user.getPassword());
            pstmt.setObject(2, user.getId());
        };
        jdbcTemplate.update(sql, setter);
    }

    @Override
    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        RowMapper<List<User>> rowMapper = rs -> {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(
                        new User(
                                rs.getLong(1),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getString(4)
                        )
                );
            }
            return users;
        };

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<User> findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        PreparedStatementSetter setter = pstmt -> pstmt.setLong(1, id);
        RowMapper<User> rowMapper = rs -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)
        );

        User user = jdbcTemplate.query(sql, setter, rowMapper);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    @Override
    public Optional<User> findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        PreparedStatementSetter setter = pstmt -> pstmt.setString(1, account);
        RowMapper<User> rowMapper = rs -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)
        );

        User user = jdbcTemplate.query(sql, setter, rowMapper);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
