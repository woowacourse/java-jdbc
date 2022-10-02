package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.ResultSetStrategyForObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";

        jdbcTemplate.executeUpdate(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            return pstmt;
        });
    }

    public void update(final User user) {
        // todo
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        jdbcTemplate.executeUpdate(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, String.valueOf(user.getId()));
            return pstmt;
        });
    }

    public List<User> findAll() {
        // todo
        final var sql = "SELECT id, account, password, email FROM users";

        List<Object> objects = jdbcTemplate.queryForList(sql, rs -> {
            List<Object> result = new ArrayList<>();
            while (rs.next()) {

                int id = Integer.parseInt(rs.getString("id"));
                String account = rs.getString("account");
                String password = rs.getString("password");
                String email = rs.getString("email");
                result.add(new User(id, account, password, email));
            }
            return result;
        });

        return objects.stream()
                .map(object -> (User) object)
                .collect(Collectors.toList());
    }

    public User findById(final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";

        return (User) jdbcTemplate.queryForObject(sql, new ResultSetStrategyForObject() {
            @Override
            public void setParameters(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            public Object mapRows(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }
        });
    }

    public User findByAccount(final String account) {
        // todo
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";

        return (User) jdbcTemplate.queryForObject(sql, new ResultSetStrategyForObject() {
            @Override
            public void setParameters(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            public Object mapRows(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }
        });
    }
}
