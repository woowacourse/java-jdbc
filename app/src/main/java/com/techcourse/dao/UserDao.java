package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            void setParams(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }

            @Override
            Object mapFromRow(ResultSet rs) throws SQLException {
                return null;
            }
        };
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.execute(sql);
    }

    public void update(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            void setParams(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }

            @Override
            Object mapFromRow(ResultSet rs) throws SQLException {
                return null;
            }
        };
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.execute(sql);
    }

    public List<User> findAll() {
        JdbcTemplate selectJdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            void setParams(PreparedStatement pstmt) {
            }

            @Override
            Object mapFromRow(ResultSet rs) throws SQLException {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)));
                }
                return users;
            }
        };
        final String sql = "select * from users";
        return (List<User>) selectJdbcTemplate.query(sql);
    }

    public User findById(Long id) {
        JdbcTemplate selectJdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            void setParams(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            Object mapFromRow(ResultSet rs) throws SQLException {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }
        };
        final String sql = "select * from users where id = ?";
        return (User) selectJdbcTemplate.query(sql);
    }

    public User findByAccount(String account) {
        JdbcTemplate selectJdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            void setParams(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            Object mapFromRow(ResultSet rs) throws SQLException {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }
        };
        final String sql = "select * from users where account = ?";
        return (User) selectJdbcTemplate.query(sql);
    }

    public void clear() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            void setParams(PreparedStatement pstmt) {
            }

            @Override
            Object mapFromRow(ResultSet rs) throws SQLException {
                return null;
            }
        };
        String sql = "drop table users";
        jdbcTemplate.execute(sql);
    }
}
