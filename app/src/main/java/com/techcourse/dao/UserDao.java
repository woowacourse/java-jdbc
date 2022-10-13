package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER =
            resultSet -> new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(final User user) {
        return jdbcTemplate.command(
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail()
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.queryForList(
                "select id, account, password, email from users",
                USER_ROW_MAPPER
        );
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForOne(
                "select id, account, password, email from users where id = ?",
                USER_ROW_MAPPER,
                id
        );
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.queryForOne(
                "select id, account, password, email from users where account = ?",
                USER_ROW_MAPPER,
                account
        );
    }

    public int update(final User user) {
        return jdbcTemplate.command(
                "update users set password = ? where id = ?",
                user.getPassword(), user.getId()
        );
    }
}
