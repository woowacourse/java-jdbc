package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... params) throws DataAccessException {
        log.debug("query : {}", sql);
        execute(sql, new PreparedStatementCreator(), ps -> {
            doSetValue(params, ps);
            ps.execute();
            return null;
        });
    }

    public int update(final String sql, final Object... params) throws DataAccessException {
        log.debug("query : {}", sql);
        return updateCount(execute(
                sql,
                new PreparedStatementCreator(),
                ps -> {
                    doSetValue(params, ps);
                    return ps.executeUpdate();
                }
        ));
    }

    private int updateCount(@Nullable final Integer result) throws DataAccessException {
        if (result == null) {
            throw new DataAccessException("no update count");
        }
        return result;
    }

    @Nullable
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) throws DataAccessException {
        final var results = query(sql, rowMapper, params);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) throws DataAccessException {
        log.debug("query : {}", sql);
        return results(execute(
                sql,
                new PreparedStatementCreator(),
                ps -> {
                    doSetValue(params, ps);
                    final var rs = ps.executeQuery();
                    final var extractor = new ResultSetExtractor<>(rs, rowMapper);
                    return extractor.extractData();
                }
        ));
    }

    private <T> List<T> results(@Nullable final List<T> results) throws DataAccessException {
        if (results == null) {
            throw new DataAccessException("no result");
        }
        return results;
    }

    private void doSetValue(final Object[] params, final PreparedStatement ps) throws SQLException {
        for (int i = 1; i <= params.length; i++) {
            ps.setObject(i, params[i - 1]);
        }
    }

    @Nullable
    private <T> T execute(
            final String sql,
            final PreparedStatementCreator psc,
            final PreparedStatementCallback<T> action
    ) throws DataAccessException {
        try (
                final var conn = getConnection();
                final var ps = psc.createPreparedStatement(conn, sql)
        ) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }
}
