package com.techcourse.dao;

import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.exception.UserNotFoundException;
import com.techcourse.repository.UserRowMapper;
import com.techcourse.repository.UsersRowMapper;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<Optional<User>> USER_MAPPER = new UserRowMapper();
    private static final RowMapper<List<User>> USERS_MAPPER = new UsersRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        final PreparedStatementSetter pstmtSetter = preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
        };

        jdbcTemplate.update(sql, pstmtSetter);
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final PreparedStatementSetter pstmtSetter = preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setLong(4, user.getId());
        };
        jdbcTemplate.update(sql, pstmtSetter);
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email from users";
        final PreparedStatementSetter pstmtSetter =
            preparedStatement -> {};

        return jdbcTemplate.query(sql, pstmtSetter, USERS_MAPPER);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        final PreparedStatementSetter pstmtSetter =
            preparedStatement -> preparedStatement.setObject(1, id);

        return jdbcTemplate.query(sql, pstmtSetter, USER_MAPPER)
            .orElseThrow(() -> new UserNotFoundException("user not found (id: " + id));
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT id, account, password, email from users where account = ?";
        final PreparedStatementSetter pstmtSetter =
            preparedStatement -> preparedStatement.setObject(1, account);

        return jdbcTemplate.query(sql, pstmtSetter, USER_MAPPER)
            .orElseThrow(() -> new UserNotFoundException("user not found (account: " + account));
    }
}
