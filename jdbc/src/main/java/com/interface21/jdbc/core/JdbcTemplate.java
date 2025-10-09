package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public <T> List<T> query(
            final String sql,
            final PreparedStatementSetter pss,
            final RowMapperResultSetExtractor<T> rse
    ) {
        return execute(sql, (pstmt) -> {
            pss.setValues(pstmt);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return rse.extractData(rs);
            }
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, new ArgumentPreparedStatementSetter(args), new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, new ArgumentPreparedStatementSetter(args),
                new RowMapperResultSetExtractor<>(rowMapper));
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, (pstmt) -> {
                    pss.setValues(pstmt);
                    return pstmt.executeUpdate();
                }
        );
    }

    public int update(final String sql, final Object... args) {
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            return callback.doInPreparedStatement(pstmt);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
