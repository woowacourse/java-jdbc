package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class UserDaoImpl implements UserDao {

    private final DataSource dataSource;

    public UserDaoImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(User user) {
        JdbcTemplate insertJdbcTemplate = new JdbcTemplate() {

            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setObject(1, user.getAccount());
                pstmt.setObject(2, user.getPassword());
                pstmt.setObject(3, user.getEmail());
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        insertJdbcTemplate.update();
    }

    @Override
    public void update(User user) {
        JdbcTemplate updateJdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set password = ? where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setObject(1, user.getPassword());
                pstmt.setObject(2, user.getId());
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        updateJdbcTemplate.update();
    }

    @Override
    public List<User> findAll() {
        JdbcTemplate selectJdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(
                            new User(
                                    rs.getLong(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getString(4)
                            )
                    );
                }
                return users;
            }
        };

        Object result = selectJdbcTemplate.query();
        return (List<User>) result;
    }

    @Override
    public Optional<User> findById(final Long id) {
        JdbcTemplate selectJdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                );
            }
        };

        Object result = selectJdbcTemplate.query();
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of((User) result);
    }

    @Override
    public Optional<User> findByAccount(final String account) {
        JdbcTemplate selectJdbcTemplate = new JdbcTemplate() {

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                );
            }
        };

        Object result = selectJdbcTemplate.query();
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of((User) result);
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
