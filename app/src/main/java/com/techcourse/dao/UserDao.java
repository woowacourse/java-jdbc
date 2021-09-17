package com.techcourse.dao;

import com.techcourse.dao.jdbc.template.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        jdbcTemplate.update();
    }

    private DataSource getDataSource() {
        return this.dataSource;
    }

    public void update(User user) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        jdbcTemplate.update();
    }

    public List<User> findAll() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
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
            }
        };

        return (List<User>) jdbcTemplate.query();
    }

    public User findById(Long id) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                }
                return null;
            }
        };

        return (User) jdbcTemplate.query();
    }

    public User findByAccount(String account) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                }
                return null;
            }
        };

        return (User) jdbcTemplate.query();
    }
}
