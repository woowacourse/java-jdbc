package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        jdbcTemplate.update();
    }

    public void update(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public String createQuery() {
                return "update users set account=?, password=?, email=? where id=?";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        jdbcTemplate.update();
    }

    public List<User> findAll() {

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {

            }

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                List<User> result = new ArrayList<>();

                while(rs.next()) {
                    result.add(new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)));
                }

                return result;
            }
        };

        return (List<User>) jdbcTemplate.query();
    }

    public User findById(Long id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                if(rs.next()) {
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

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                if(rs.next()) {
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
