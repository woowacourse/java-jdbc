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
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }
        };

        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        PreparedStatementSetter setter = new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setObject(1, user.getAccount());
                pstmt.setObject(2, user.getPassword());
                pstmt.setObject(3, user.getEmail());
            }
        };

        insertJdbcTemplate.update(sql, setter);
    }

    @Override
    public void update(User user) {
        JdbcTemplate updateJdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }
        };

        String sql = "update users set password = ? where id = ?";
        PreparedStatementSetter setter = new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setObject(1, user.getPassword());
                pstmt.setObject(2, user.getId());
            }
        };

        updateJdbcTemplate.update(sql, setter);
    }

    @Override
    public List<User> findAll() {
        JdbcTemplate selectJdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }
        };

        String sql = "select id, account, password, email from users";
        PreparedStatementSetter setter = new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
            }
        };

        RowMapper rowMapper = new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
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

        Object result = selectJdbcTemplate.query(sql, setter, rowMapper);
        return (List<User>) result;
    }

    @Override
    public Optional<User> findById(final Long id) {
        JdbcTemplate selectJdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }
        };

        String sql = "select id, account, password, email from users where id = ?";
        PreparedStatementSetter setter = new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }
        };
        RowMapper rowMapper = new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                );
            }
        };

        Object result = selectJdbcTemplate.query(sql, setter, rowMapper);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of((User) result);
    }

    @Override
    public Optional<User> findByAccount(final String account) {
        JdbcTemplate selectJdbcTemplate = new JdbcTemplate() {

            @Override
            protected DataSource getDataSource() {
                return UserDaoImpl.this.getDataSource();
            }
        };

        String sql = "select id, account, password, email from users where account = ?";
        PreparedStatementSetter setter = new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }
        };
        RowMapper rowMapper = new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                );
            }
        };

        Object result = selectJdbcTemplate.query(sql, setter, rowMapper);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of((User) result);
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
