package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.PreparedStatementSetter;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> rowMapper = rs -> {
        if (rs.next()) {
            return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
        }
        return null;
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        PreparedStatementSetter pstmtSetter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };

        jdbcTemplate.update(sql, pstmtSetter);
    }

    public void update(User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";
        PreparedStatementSetter pstmtSetter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        };

        jdbcTemplate.update(sql, pstmtSetter);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        PreparedStatementSetter pstmtSetter = pstmt -> {};

        return jdbcTemplate.query(sql, pstmtSetter, rowMapper);
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        PreparedStatementSetter pstmtSetter = pstmt -> pstmt.setLong(1, id);

        return jdbcTemplate.queryForObject(sql, pstmtSetter, rowMapper);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        PreparedStatementSetter pstmtSetter = pstmt -> pstmt.setString(1, account);

        return jdbcTemplate.queryForObject(sql, pstmtSetter, rowMapper);
    }
}
