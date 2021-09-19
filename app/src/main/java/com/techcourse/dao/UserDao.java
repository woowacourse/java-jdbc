package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.JdbcTemplateException;
import nextstep.jdbc.SelectJdbcTemplate;
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
    private static final String QUERY_LOG = "query : {}";

    private static final String ID = "id";
    private static final String ACCOUNT = "account";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }
        };
        jdbcTemplate.update();
    }

    public void update(User user) {
        jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set account=?, password=?, email=? where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }
        };
        jdbcTemplate.update();
    }

    public List<User> findAll() {
        SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select * from users";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected Object mapRow(final ResultSet rs) throws SQLException {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    long id = rs.getLong(ID);
                    String account = rs.getString(ACCOUNT);
                    String password = rs.getString(PASSWORD);
                    String email = rs.getString(EMAIL);
                    User user = new User(id, account, password, email);
                    users.add(user);
                }
                return users;
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) {
                // Do nothing...
            }
        };
        return (List<User>) jdbcTemplate.query();
    }

    public User findById(Long id) {
        SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected Object mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(ID),
                            rs.getString(ACCOUNT),
                            rs.getString(PASSWORD),
                            rs.getString(EMAIL));
                }
                return null;
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }
        };
        return (User) jdbcTemplate.query();
}

    public User findByAccount(String account) {
        SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select * from users where account = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected Object mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    long id = rs.getLong(ID);
                    String password = rs.getString(PASSWORD);
                    String email = rs.getString(EMAIL);
                    return new User(id, account, password, email);
                }
                return null;
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }
        };
        return (User) jdbcTemplate.query();
    }
}
