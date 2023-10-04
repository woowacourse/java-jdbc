package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> rowMapper =
            rs -> new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Connection connection, User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        log.debug("query : {}", sql);

        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(Connection connection, User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        log.debug("query : {}", sql);

        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "SELECT * from users";

        log.debug("query : {}", sql);

        return jdbcTemplate.query(sql, rowMapper);
    }

    public User findById(Long id) {
        String sql = "select * from users where id = ?";

        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    public User findByAccount(String account) {
        String sql = "select * from users where account = ?";

        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(sql, rowMapper, account);
    }

}
