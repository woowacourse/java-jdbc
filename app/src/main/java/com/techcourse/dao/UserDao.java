package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper =
            rs -> new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        int insertCount = jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
        log.info("insert 영향을 받은 row 수: {}", insertCount);
    }

    // 기존 버전 (호환성을 위해서 유지)
    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        int updateRowsCount = jdbcTemplate.executeUpdate(
                sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        log.info("update 영향을 받은 row 수: {}", updateRowsCount);
    }

    // connection 버전
    public void update(final Connection connection, final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        int updateRowsCount = jdbcTemplate.executeUpdate(
                connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        log.info("update 영향을 받은 row 수: {}", updateRowsCount);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.executeQuery(sql, userRowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.executeQueryForObject(sql, userRowMapper, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.executeQueryForObject(sql, userRowMapper, account);
    }
}
