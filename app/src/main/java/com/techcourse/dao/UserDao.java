package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final String QUERY_SQL = "query : {}";

    private static final String ID = "id";
    private static final String ACCOUNT = "account";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        JdbcTemplate<Void> jdbcTemplate = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                String sql = "insert into users (account, password, email) values (?, ?, ?)";
                log.debug(QUERY_SQL, sql);
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public Void mapRow(final ResultSet rs) {
                return null;
            }
        };
        jdbcTemplate.update(user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        JdbcTemplate<Void> jdbcTemplate = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                String sql =  "update users set account=?, password=?, email=? where id = ?";
                log.debug(QUERY_SQL, sql);
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public Void mapRow(final ResultSet rs) {
                return null;
            }
        };
        jdbcTemplate.update(user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        JdbcTemplate<List<User>> jdbcTemplate = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                String sql = "select * from users";
                log.debug(QUERY_SQL, sql);
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public List<User> mapRow(final ResultSet rs) throws SQLException {
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
        };
        return jdbcTemplate.query();
    }

    public User findById(Long id) {
        JdbcTemplate<User> jdbcTemplate = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                String sql = "select id, account, password, email from users where id = ?";
                log.debug(QUERY_SQL, sql);
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public User mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(ID),
                            rs.getString(ACCOUNT),
                            rs.getString(PASSWORD),
                            rs.getString(EMAIL));
                }
                return null;
            }
        };
        return jdbcTemplate.query(id);
    }

    public User findByAccount(String account) {
        JdbcTemplate<User> jdbcTemplate = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                String sql = "select * from users where account = ?";
                log.debug(QUERY_SQL, sql);
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public User mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    long id = rs.getLong(ID);
                    String password = rs.getString(PASSWORD);
                    String email = rs.getString(EMAIL);
                    return new User(id, account, password, email);
                }
                return null;
            }
        };
        return jdbcTemplate.query(account);
    }
}
