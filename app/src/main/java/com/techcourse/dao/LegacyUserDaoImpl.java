package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.LegacyJdbcTemplate;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyUserDaoImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(LegacyUserDaoImpl.class);

    private final LegacyJdbcTemplate legacyJdbcTemplate;

    public LegacyUserDaoImpl(final LegacyJdbcTemplate legacyJdbcTemplate) {
        this.legacyJdbcTemplate = legacyJdbcTemplate;
    }

    @Override
    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        legacyJdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    @Override
    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";
        legacyJdbcTemplate.executeUpdate(sql, user.getPassword(), user.getId());
    }

    @Override
    public void update(final Connection conn,  final User user) {
        final var sql = "update users set password = ? where id = ?";
        legacyJdbcTemplate.executeUpdate(sql, user.getPassword(), user.getId());
    }

    @Override
    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return legacyJdbcTemplate.executeQueryWithMultiData(sql, this::generateUser);
    }

    @Override
    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return legacyJdbcTemplate.executeQueryWithSingleData(sql, this::generateUser, id);
    }

    @Override
    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return legacyJdbcTemplate.executeQueryWithSingleData(sql, this::generateUser, account);
    }

    @Override
    public DataSource getDataSource() {
        return null;
    }

    private User generateUser(ResultSet rs) {
        try {
            return new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException("user 데이터 추출 실패", exception);
        }
    }
}
