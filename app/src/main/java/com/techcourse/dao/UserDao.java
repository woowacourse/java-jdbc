package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private final DataSource dataSource;
//    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (resultSet) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        jdbcTemplate.update();
    }

    public void update(final User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set password = ? where id = ?";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getPassword());
                pstmt.setLong(2, user.getId());
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        jdbcTemplate.update();
    }


    public List<User> findAll() {
//        final var sql = "select id, account, password, email from users";
        return null;
    }

    public User findById(final Long id) {
//        final var sql = "select id, account, password, email from users where id = ?";
        return null;
    }

    public User findByAccount(final String account) {
//        final var sql = "select id, account, password, email from users where account = ?";
        return null;
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
