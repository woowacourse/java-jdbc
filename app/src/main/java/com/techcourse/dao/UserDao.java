package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final ResultSetMapper<User> RESULT_SET_MAPPER = resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

//        Connection conn = null;
//        PreparedStatement pstmt = null;
//        try {
//            conn = jdbcTemplate.getConnection();
//            pstmt = conn.prepareStatement(sql);
//
//            log.debug("query : {}", sql);
//
//            pstmt.setString(1, user.getAccount());
//            pstmt.setString(2, user.getPassword());
//            pstmt.setString(3, user.getEmail());
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                if (pstmt != null) {
//                    pstmt.close();
//                }
//            } catch (SQLException ignored) {}
//
//            try {
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (SQLException ignored) {}
//        }
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, RESULT_SET_MAPPER, id);
    }

    public User findByAccount(final String account) {
        // todo
        return null;
    }
}
