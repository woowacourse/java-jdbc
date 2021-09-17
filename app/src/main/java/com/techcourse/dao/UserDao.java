package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.PreparedStatementSetter;
import nextstep.jdbc.RowMapper;

public class UserDao {
    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        PreparedStatementSetter pstmtSetter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };
        JdbcTemplate jdbcTemplate = new JdbcTemplate(pstmtSetter){
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };
        jdbcTemplate.update();
    }

    public void update(User user) {
        PreparedStatementSetter pstmtSetter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        };
        JdbcTemplate jdbcTemplate = new JdbcTemplate(pstmtSetter) {
            @Override
            protected String createQuery() {
                return "update users SET account=?, password=?, email=? where id =?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };
        jdbcTemplate.update();
    }

    public List<User> findAll() {
        RowMapper rowMapper = rs -> {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)));
            }
            return users;
        };
        JdbcTemplate jdbcTemplate = new JdbcTemplate(rowMapper) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };
        return (List<User>) jdbcTemplate.query();
    }

    public User findById(Long id) {
        PreparedStatementSetter pstmtSetter = pstmt -> pstmt.setLong(1, id);

        RowMapper rowMapper = rs -> {
            if (rs.next()) {
                return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
            }
            return null;
        };

        JdbcTemplate jdbcTemplate = new JdbcTemplate(pstmtSetter, rowMapper) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };
        return (User) jdbcTemplate.query();
    }

    public User findByAccount(String account) {
        PreparedStatementSetter pstmtSetter = pstmt -> pstmt.setString(1, account);

        RowMapper rowMapper = rs -> {
            if (rs.next()) {
                return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
            }
            return null;
        };

        JdbcTemplate jdbcTemplate = new JdbcTemplate(pstmtSetter, rowMapper) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };
        return (User) jdbcTemplate.query();
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
