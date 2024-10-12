package com.techcourse.dao;

import com.interface21.jdbc.ObjectMapper;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper<User> objectMapper = (rs) -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
    );

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
        log.debug("query : {}", sql);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        log.debug("query : {}", sql);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        List<List<Object>> results = jdbcTemplate.queryList(sql);

        List<User> users = new ArrayList<>();
        for (List<Object> result : results) {
            User user = new User(
                    (Long) result.get(0),
                    (String) result.get(1),
                    (String) result.get(2),
                    (String) result.get(3)
            );
            users.add(user);
        }
        return users;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        List<Object> result = jdbcTemplate.query(sql, id);

        return new User(
                (Long) result.get(0),
                (String) result.get(1),
                (String) result.get(2),
                (String) result.get(3)
        );
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        List<Object> result = jdbcTemplate.query(sql, account);
        return new User(
                (Long) result.get(0),
                (String) result.get(1),
                (String) result.get(2),
                (String) result.get(3)
        );
    }
}


