package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final Function<ResultSet, User> rowMapper =
            resultSet -> {
                try {
                    return new User(
                            resultSet.getLong("id"),
                            resultSet.getString("account"),
                            resultSet.getString("password"),
                            resultSet.getString("email")
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };

    private DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ?";
        jdbcTemplate.update(
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public List<User> findAll() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public User findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        List<User> user = jdbcTemplate.query(sql, rowMapper, id);

        if (user.size() != 1) {
            throw new RuntimeException("조회 결과가 단일이 아닙니다.");
        }

        return user.get(0);
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        List<User> user = jdbcTemplate.query(sql, rowMapper, account);

        if (user.size() != 1) {
            throw new RuntimeException("조회 결과가 단일이 아닙니다.");
        }

        return user.get(0);
    }

}
