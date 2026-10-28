package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = """
                insert into users (account, password, email)
                values (?, ?, ?)
                """;

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = """
                update users
                set account = ?, password = ?, email = ?
                where id = ?
                """;

        jdbcTemplate.update(
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public void update(final Connection connection, final User user) {
        final String sql = """
                update users
                set account = ?, password = ?, email = ?
                where id = ?
                """;

        final int updatedRows = jdbcTemplate.update(
                connection,
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );

        if (updatedRows != 1) {
            throw new DataAccessException("사용자 수정 결과는 1건이어야 합니다. 실제 수정 건수: " + updatedRows);
        }
    }

    public List<User> findAll() {
        final String sql = """
                select id, account, password, email
                from users
                """;

        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final String sql = """
                select id, account, password, email
                from users
                where id = ?
                """;

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByIdForUpdate(final Connection connection, final Long id) {
        final String sql = """
                select id, account, password, email
                from users
                where id = ?
                for update
                """;

        return jdbcTemplate.queryForObject(connection, sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final String sql = """
                select id, account, password, email
                from users
                where account = ?
                """;

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
