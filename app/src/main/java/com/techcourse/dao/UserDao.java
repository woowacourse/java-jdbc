package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    // AS-IS
    private final DataSource dataSource;

    // TO-BE
    private final JdbcTemplate jdbcTemplate;

    // AS-IS
//    public UserDao(final DataSource dataSource) {
//        this.dataSource = dataSource;
//    }

    // TO-BE
    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }

    public void insert(final User user) {
        log.info("insert user : {}", user.toString());
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        log.info("update user : {}", user.toString());
        final var sql = "UPDATE users SET account = ?, password = ?, email =? WHERE id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, (ResultSet rs, int rowNum) ->
                        new User(
                                rs.getLong("id"),
                                rs.getString("account"),
                                rs.getString("password"),
                                rs.getString("email"))
                , id);
    }

    public User findByAccount(final String account) {
        // todo
        return null;
    }
}
