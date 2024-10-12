package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final int SINGLE_RESULT = 1;
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, new PreparedStatementSetter(args));
    }

    public int update(final Connection conn, final String sql, final Object... args) {
        return execute(conn, sql, PreparedStatement::executeUpdate, new PreparedStatementSetter(args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final var results = query(sql, rowMapper, args);

        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(SINGLE_RESULT);
        }
        if (results.size() > SINGLE_RESULT) {
            throw new IncorrectResultSizeDataAccessException(SINGLE_RESULT, results.size());
        }
        return results.getFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> executeQuery(pstmt, rowMapper), new PreparedStatementSetter(args));
    }

    private <T> List<T> executeQuery(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        final var resultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
        try (ResultSet rs = pstmt.executeQuery()) {
            return resultSetExtractor.extractData(rs);
        }
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> action, final PreparedStatementSetter pss) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setValues(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final Connection conn, final String sql, final PreparedStatementCallback<T> action, final PreparedStatementSetter pss) {
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setValues(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
