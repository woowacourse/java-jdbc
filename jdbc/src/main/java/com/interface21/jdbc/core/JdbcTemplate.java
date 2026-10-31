package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        return execute(sql, createPreparedStatementSetter(params), PreparedStatement::executeUpdate);
    }

    public int update(final Connection connection, final String sql, final Object... params) {
        return execute(connection, sql, createPreparedStatementSetter(params), PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... params
    ) {
        return execute(sql, createPreparedStatementSetter(params), createQueryForObjectCallback(rowMapper));
    }

    public <T> T queryForObject(
            final Connection connection,
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... params
    ) {
        return execute(connection, sql, createPreparedStatementSetter(params), createQueryForObjectCallback(rowMapper));
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... params
    ) {
        return execute(sql, createPreparedStatementSetter(params), pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();

                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementSetter statementSetter,
            final PreparedStatementCallback<T> callback
    ) {
        try (Connection conn = dataSource.getConnection()) {
            return execute(conn, sql, statementSetter, callback);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행에 실패했습니다.", e);
        }
    }

    private <T> T execute(
            final Connection connection,
            final String sql,
            final PreparedStatementSetter statementSetter,
            final PreparedStatementCallback<T> callback
    ) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            statementSetter.setValues(pstmt);
            return callback.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행에 실패했습니다.", e);
        }
    }

    private <T> PreparedStatementCallback<T> createQueryForObjectCallback(final RowMapper<T> rowMapper) {
        return pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            }
        };
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... params) {
        return pstmt -> {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        };
    }
}
