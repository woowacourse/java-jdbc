package com.interface21.jdbc.core;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.EmptyResultDataAccessException;
import com.interface21.jdbc.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return executeQuery(sql, (pstmt) -> executeAndGet(pstmt, rowMapper));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        return executeQuery(sql, (pstmt) -> {
            bindValues(pstmt, values);
            return executeAndGet(pstmt, rowMapper);
        });
    }

    private <T> T executeQuery(String sql, PreparedStatementCallBack<T> callBack) {
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return callBack.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> executeAndGet(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        List<T> result = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs, rs.getRow()));
            }
        }
        return result;
    }

    private void bindValues(PreparedStatement pstmt, Object... values) throws SQLException {
        for (int idx = 1; idx <= values.length; idx++) {
            pstmt.setObject(idx, values[idx - 1]);
        }
    }

    public <T> T queryForObject(String sql, Class<T> clazz, Object... values) {
        RowMapper<T> rowMapper = RowMapperFactory.getRowMapper(clazz);
        List<T> result = query(sql, rowMapper, values);
        validateSingleResult(result);
        return result.getFirst();
    }

    private <T> void validateSingleResult(List<T> result) {
        if (result.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, result.size());
        }
        if (result.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
    }

    public void update(String sql, Object... values) {
        executeQuery(sql, (pstmt) -> {
            bindValues(pstmt, values);
            return pstmt.executeUpdate();
        });
    }

    public void execute(String sql) {
        executeQuery(sql, PreparedStatement::execute);
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }
}
