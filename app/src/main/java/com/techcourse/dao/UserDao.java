package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.User;

import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        update(sql, pstmt -> {
                    pstmt.setString(1, user.getAccount());
                    pstmt.setString(2, user.getPassword());
                    pstmt.setString(3, user.getEmail());
                }
        );
    }

    private void update(final String sql, final PreparedStatementSetter ps) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            ps.set(pstmt);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    interface PreparedStatementSetter {
        void set(final PreparedStatement pstmt) throws SQLException;
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        update(sql, pstmt -> {
                    pstmt.setString(1, user.getAccount());
                    pstmt.setString(2, user.getPassword());
                    pstmt.setString(3, user.getEmail());
                    pstmt.setLong(4, user.getId());
                }
        );
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return query(sql,
                rs -> new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4))
        );
    }

    private <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return execute(pstmt, rowMapper);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return queryForObject(sql,
                pstmt -> pstmt.setLong(1, id),
                rs -> new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4))
        );
    }

    private <T> T queryForObject(final String sql, final PreparedStatementSetter ps, final RowMapper<T> rowMapper) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            ps.set(pstmt);

            final List<T> results = execute(pstmt, rowMapper);
            return nullableSingleResult(results);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> execute(final PreparedStatement pstmt, final RowMapper<T> rowMapper) {
        try (final ResultSet rs = pstmt.executeQuery()) {
            return extract(rs, rowMapper);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> extract(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (rs.next()) {
            final T result = rowMapper.mapTow(rs);
            results.add(result);
        }
        return results;
    }

    private <T> T nullableSingleResult(List<T> results) {
        if (results == null || results.isEmpty()) {
            throw new IncorrectResultSizeDataAccessException(1, 0);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.iterator().next();
    }

    class IncorrectResultSizeDataAccessException extends DataAccessException {
        public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
            super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
        }
    }

    @FunctionalInterface
    interface RowMapper<T> {
        T mapTow(final ResultSet rs) throws SQLException;
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return queryForObject(sql,
                pstmt -> pstmt.setString(1, account),
                rs -> new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4))
        );
    }
}
