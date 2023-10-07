package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        query(sql, PreparedStatement::executeUpdate, args);
    }

    public void update(final Connection connection, final String sql, final Object... args) {
        queryWithConnection(connection, sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args) {
        List<T> results = queryForList(sql, mapper, args);
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException("조회 결과가 존재하지 않습니다.");
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataException("조회 결과가 1건 이상입니다.");
        }
        return results.get(0);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... args) {
        return query(sql, statement -> {
            try (ResultSet rs = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
                return results;
            }
        }, args);
    }

    private <T> T query(
            final String sql,
            final JdbcCallback<T> jdbcCallback,
            final Object... args
    ) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = getPreparedStatement(conn, sql, args)) {
            log.debug("query : {}", sql);
            return jdbcCallback.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> T queryWithConnection(
            final Connection connection,
            final String sql,
            final JdbcCallback<T> jdbcCallback,
            final Object... args
    ) {
        try (final PreparedStatement preparedStatement = getPreparedStatement(connection, sql, args)) {
            log.debug("query : {}", sql);
            return jdbcCallback.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement getPreparedStatement(final Connection conn, final String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }
}
