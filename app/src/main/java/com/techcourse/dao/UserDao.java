package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;

public class UserDao {

    private final DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(User user) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "update users set password = ? where id = ?";

        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getPassword());
            pstmt.setLong(2, user.getId());
        });
    }

    public List<User> findAll() {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "select id, account, password, email from users";

        return (List<User>) jdbcTemplate.query(
            sql,
            rs -> {
                List<User> users = new ArrayList<>();

                while (rs.next()) {
                    users.add(new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                    ));
                }

                return users;
            });
    }

    public User findById(Long id) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "select id, account, password, email from users where id = ?";

        return (User) jdbcTemplate.queryForObject(
            sql,
            pstmt -> pstmt.setLong(1, id),
            rs -> {
                if (rs.next()) {
                    return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                }
                return null;
            });
    }

    public User findByAccount(String account) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        String sql = "select id, account, password, email from users where account = ?";

        return (User) jdbcTemplate.queryForObject(
            sql,
            pstmt -> pstmt.setString(1, account),
            rs -> {
                if (rs.next()) {
                    return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                }
                return null;
            });
    }
}
