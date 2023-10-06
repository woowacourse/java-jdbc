package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private final RowMapper<User> mapper = resultSet -> {
        final long id = resultSet.getLong("id");
        final String account = resultSet.getString("account");
        final String password = resultSet.getString("password");
        final String email = resultSet.getString("email");

        return new User(id, account, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final Connection conn, final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.execute(conn, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection conn,final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.execute(conn, sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll(final Connection conn) {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.queryForObjects(conn, sql, mapper);
    }

    public User findById(final Connection conn, final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(conn, sql, mapper, id);
    }

    public User findByAccount(final Connection conn, final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(conn, sql, mapper, account);
    }

    public void deleteByAccount(final Connection conn, final String gugu) {
        final var sql = "delete from users where account = ?";
        jdbcTemplate.execute(conn, sql, gugu);
    }
}
