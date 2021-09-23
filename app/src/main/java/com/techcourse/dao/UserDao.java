package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> userRowMapper = (rs) -> new User(
        rs.getLong(1),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4));

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate
            .update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select * from users";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {
            List<User> users = new ArrayList<>();

            log.debug("query : {}", sql);

            while (rs.next()) {
                User user = new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
                users.add(user);
            }
            return users;
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage(), sqlException);
            throw new RuntimeException();
        }
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, account);
    }
}
