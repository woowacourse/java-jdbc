package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final String UPDATE_SQL =
            "update users set password = ? where id = ?";

    private static final String FIND_BY_ID_SQL =
            "select id, account, password, email from users where id = ?";

    private static final RowMapper<User> USER_ROW_MAPPER =
            rs -> new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        try {
            jdbcTemplate.update(
                    sql,
                    user.getAccount(),
                    user.getPassword(),
                    user.getEmail()
            );
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())) {
                throw new DuplicateAccountException(user.getAccount(), e);
            }
            throw e;
        }
    }

    public void update(User user) {
        int updatedRows = jdbcTemplate.update(
                UPDATE_SQL,
                user.getPassword(),
                user.getId()
        );

        validateUpdatedRows(user.getId(), updatedRows);
    }

    private void validateUpdatedRows(long id, int updatedRows) {
        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "사용자 수정 실패: id=" + id
                            + ", updatedRows=" + updatedRows
            );
        }
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(Long id) {
        return jdbcTemplate.queryForObject(
                FIND_BY_ID_SQL,
                USER_ROW_MAPPER,
                id
        );
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
