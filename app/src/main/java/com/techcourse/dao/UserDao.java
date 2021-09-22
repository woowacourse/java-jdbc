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
        JdbcTemplate insertJdbcTemplate = new JdbcTemplate() {
            @Override
            String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            DataSource getDataSource() {
                return dataSource;
            }

            @Override
            void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }

            @Override
            Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        insertJdbcTemplate.update();
    }

    public void update(User user) {
        JdbcTemplate updateJdbcTemplate = new JdbcTemplate() {
            @Override
            String createQuery() {
                return "update users set account=?, password=?, email=? where id=?";
            }

            @Override
            DataSource getDataSource() {
                return dataSource;
            }

            @Override
            void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }

            @Override
            Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        updateJdbcTemplate.update();
    }

    public List<User> findAll() {
        JdbcTemplate selectAllJdbcTemplate = new JdbcTemplate() {
            @Override
            String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            DataSource getDataSource() {
                return dataSource;
            }

            @Override
            Object mapRow(ResultSet rs) throws SQLException {
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

            @Override
            void setValues(PreparedStatement pstmt) throws SQLException {
            }
        };

        return (List<User>) selectAllJdbcTemplate.query();
    }

    public User findById(Long id) {
        JdbcTemplate selectByIdJdbcTemplate = new JdbcTemplate() {
            @Override
            String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            DataSource getDataSource() {
                return dataSource;
            }

            @Override
            Object mapRow(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }

            @Override
            void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }
        };

        return (User) selectByIdJdbcTemplate.query();
    }

    public User findByAccount(String account) {
        JdbcTemplate selectByIdJdbcTemplate = new JdbcTemplate() {
            @Override
            String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            DataSource getDataSource() {
                return dataSource;
            }

            @Override
            Object mapRow(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }

            @Override
            void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }
        };

        return (User) selectByIdJdbcTemplate.query();
    }
}
