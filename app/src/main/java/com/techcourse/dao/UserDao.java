package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.SelectJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    private SelectJdbcTemplate selectJdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            public String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }
        };

        jdbcTemplate.update();
    }

    public void update(User user) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            public String createQuery() {
                return "update users set password = ? where id = ?";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getPassword());
                pstmt.setLong(2, user.getId());
            }
        };

        jdbcTemplate.update();
    }

    public List<User> findAll() {
        this.selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            public String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
            }

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
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
            }
        };

        return (List<User>) selectJdbcTemplate.query();
    }

    public User findById(Long id) {
        this.selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            public String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                }
                return null;
            }
        };

        return (User) selectJdbcTemplate.query();
    }

    public User findByAccount(String account) {
        this.selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            public String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                }
                return null;
            }
        };

        return (User) selectJdbcTemplate.query();
    }
}
