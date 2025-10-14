package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate implements JdbcOperations {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int update(final String sql, final Object... args) {
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    @Override
    public int update(final Connection conn, final String sql, final Object... args) {
        return update(conn, sql, new ArgumentPreparedStatementSetter(args));
    }

    @Override
    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    @Override
    public int update(final Connection conn, final String sql, final PreparedStatementSetter pss) {
        return execute(conn, sql, pstmt -> {
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    @Override
    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, new ArgumentPreparedStatementSetter(args), rowMapper);
    }

    @Override
    public <T> List<T> query(final String sql, final PreparedStatementSetter pss, final RowMapper<T> rowMapper) {
        return query(sql, pss, new RowMapperResultSetExtractor<>(rowMapper));
    }


    @Override
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, new ArgumentPreparedStatementSetter(args), rowMapper);
    }

    @Override
    public <T> T queryForObject(final String sql, final PreparedStatementSetter pss, final RowMapper<T> rowMapper) {
        final List<T> results = query(sql, pss, rowMapper);
        if (results.isEmpty()) {
            throw new DataAccessException("Incorrect result size: expected 1, actual 0");
        }
        return results.getFirst();
    }

    @Override
    public <T> T query(final String sql, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return rse.extractData(rs);
            }
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final Connection conn, final String sql, final PreparedStatementCallback<T> callback) {
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
