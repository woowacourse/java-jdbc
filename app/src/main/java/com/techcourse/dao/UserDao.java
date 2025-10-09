package com.techcourse.dao;

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

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    DataSource getDataSource() {
        return dataSource;
    }

    public void insert(final User user) {
        final JdbcTemplate insertJdbcTemplate = new JdbcTemplate() {

            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }
        };

        insertJdbcTemplate.update();
    }

    public void update(final User user) {
        final JdbcTemplate updateJdbcTemplate = new JdbcTemplate() {

            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }
        };

        updateJdbcTemplate.update();
    }

    public List<User> findAll() {
        final SelectJdbcTemplate selectJdbcTemplate = new SelectJdbcTemplate() {

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected Object mapRow(final ResultSet rs) throws SQLException {
                final List<User> users = new ArrayList<>();
                while (rs.next()) {
                    final User user = new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    );
                    users.add(user);
                }
                return users;
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
            }
        };

        return (List<User>) selectJdbcTemplate.query();
    }

    public User findById(final Long id) {
        final SelectJdbcTemplate selectJdbcTemplate = new SelectJdbcTemplate() {

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected Object mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    );
                }
                return null;
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }
        };

        return (User) selectJdbcTemplate.query();
    }

    public User findByAccount(final String account) {
        final SelectJdbcTemplate selectJdbcTemplate = new SelectJdbcTemplate() {

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected Object mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    );
                }
                return null;
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }
        };

        return (User) selectJdbcTemplate.query();
    }
}
