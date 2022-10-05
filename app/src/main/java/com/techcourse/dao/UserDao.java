package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> autoRowMapper = resultSet -> new User(
            resultSet.getInt("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

        return jdbcTemplate.query(sql, autoRowMapper);
    }

    public Optional<User> findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(sql, autoRowMapper, id);
    }

    public Optional<User> findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(sql, autoRowMapper, account);
    }
}
