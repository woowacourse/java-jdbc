package com.techcourse.dao;

import com.interface21.RowMapper;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private static final RowMapper<User> userRowMapper = (rs, row) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = jdbcTemplate.getDataSource();
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        new InsertJdbcTemplate(this).insert(user);
    }

    public void update(final User user) {
        new UpdateJdbcTemplate(this).update(user);
    }

    DataSource getDataSource() {
        return dataSource;
    }

    public List<User> findAll() {
        String sql = """
                select id, account, password, email from users;
                """;
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    public User findByAccount(final String account) {
        String sql = """
                select * from users where account = ?
                """;
        return jdbcTemplate.query(sql, userRowMapper, account).getFirst();
    }
}
