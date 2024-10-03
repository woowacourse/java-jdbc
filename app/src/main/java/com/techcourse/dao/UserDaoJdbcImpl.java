package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDaoJdbcImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoJdbcImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDaoJdbcImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    @Override
    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";
        jdbcTemplate.executeUpdate(sql, user.getPassword(), user.getId());
    }

    @Override
    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.executeQueryWithMultiData(sql, this::generateUser);
    }

    @Override
    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.executeQueryWithSingleData(sql, this::generateUser, id);
    }

    @Override
    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.executeQueryWithSingleData(sql, this::generateUser, account);
    }

    private User generateUser(ResultSet rs) {
        try {
            return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
            );
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException("user 데이터 추출 실패", exception);
        }
    }
}
