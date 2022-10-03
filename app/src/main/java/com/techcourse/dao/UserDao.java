package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final DataSource dataSource, final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    public int insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.debug("query : {}", sql);
        return jdbcTemplate.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            return pstmt;
        });
    }

    public int update(final User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        log.debug("query : {}", sql);
        return jdbcTemplate.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
            return pstmt;
        });
    }

    public List<User> findAll() {
        String sql = "select * from users";
        log.debug("query : {}", sql);
        return jdbcTemplate.queryForList(sql, (resultSet) ->
                new User(
                        resultSet.getInt("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                ));
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        log.debug("query : {}", sql);
        Optional<User> user = jdbcTemplate.queryForObject(
                (connection) -> {
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    pstmt.setLong(1, id);
                    return pstmt;
                },
                (resultSet) -> new User(
                        resultSet.getInt("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                ));
        if (user.isEmpty()) {
            throw new RuntimeException();
        }
        return user.get();
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        log.debug("query : {}", sql);
        Optional<User> user = jdbcTemplate.queryForObject(
                (connection) -> {
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    pstmt.setString(1, account);
                    return pstmt;
                },
                (resultSet) -> new User(
                        resultSet.getInt("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                ));
        if (user.isEmpty()) {
            throw new RuntimeException();
        }
        return user.get();
    }
}
