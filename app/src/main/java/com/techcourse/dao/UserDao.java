package com.techcourse.dao;

import com.techcourse.dao.jdbc.template.JdbcTemplate;
import com.techcourse.dao.jdbc.template.PreparedStatementSetter;
import com.techcourse.dao.jdbc.template.RowMapper;
import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<User> findAll() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "select id, account, password, email from users";
        final PreparedStatementSetter pstmtSetter = pstmt -> {
        };
        final RowMapper rowMapper = rs -> {
            final List<User> users = new ArrayList<>();
            while (rs.next()) {
                final User user = new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
                users.add(user);
            }
            return users;
        };

        return (List<User>) jdbcTemplate.query(sql, pstmtSetter, rowMapper);
    }

    public User findByAccount(String account) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "select id, account, password, email from users where account = ?";
        final PreparedStatementSetter pstmtSetter = pstmt -> pstmt.setString(1, account);
        final RowMapper rowMapper = rs -> {
            if (rs.next()) {
                return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
            }
            return null;
        };

        return (User) jdbcTemplate.query(sql, pstmtSetter, rowMapper);
    }

    public User findById(Long id) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "select id, account, password, email from users where id = ?";
        final PreparedStatementSetter pstmtSetter = pstmt -> pstmt.setLong(1, id);
        final RowMapper rowMapper = rs -> {
            if (rs.next()) {
                return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
            }
            return null;
        };

        return (User) jdbcTemplate.query(sql, pstmtSetter, rowMapper);
    }

    public void insert(User user) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        final PreparedStatementSetter pstmtSetter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };

        jdbcTemplate.update(sql, pstmtSetter);
    }

    public void update(User user) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final PreparedStatementSetter pstmtSetter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        };

        jdbcTemplate.update(sql, pstmtSetter);
    }
}
