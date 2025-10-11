package com.interface21.jdbc.core;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.exception.ConnectionCloseException;
import com.interface21.jdbc.exception.ParameterBindingException;
import com.interface21.jdbc.exception.QueryExecutionException;
import com.interface21.jdbc.exception.ResultSetProcessingException;
import com.interface21.jdbc.exception.RowMappingException;
import com.interface21.jdbc.exception.StatementPreparationException;
import com.interface21.jdbc.exception.UpdateExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Deprecated
    public int update(String sql, Object... params) {
        return update(sql, (pstmt) -> bindParams(pstmt, params));
    }

    public int update(String sql, PreparedStatementSetter setter) {
        try (final var conn = getConnection();
             final var pstmt = getPreparedStatement(sql, conn)) {
            log.debug("query : {}", sql);
            bindParams(sql, pstmt, setter);
            return executeUpdate(sql, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new ConnectionCloseException(e, sql);
        }
    }

    public int update(Connection connection, String sql, PreparedStatementSetter setter) {
        try (final var pstmt = getPreparedStatement(sql, connection)) {
            log.debug("query : {}", sql);
            bindParams(sql, pstmt, setter);
            return executeUpdate(sql, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new ConnectionCloseException(e, sql);
        }
    }

    @Deprecated
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return query(sql, (pstmt) -> bindParams(pstmt, params), rowMapper);
    }

    public <T> List<T> query(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) {
        final List<T> result = new ArrayList<>();

        try (final var conn = getConnection();
             final var pstmt = getPreparedStatement(sql, conn)) {
            log.debug("query : {}", sql);
            try (final var rs = executeQuery(sql, pstmt, setter)) {
                while (rs.next()) {
                    result.add(mapRow(rowMapper, rs, sql));
                }
                return result;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new ResultSetProcessingException(e, sql);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new ConnectionCloseException(e, sql);
        }
    }

    @Deprecated
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(sql, (pstmt) -> bindParams(pstmt, params), rowMapper);
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) {
        try (final var conn = getConnection();
             final var pstmt = getPreparedStatement(sql, conn)) {
            log.debug("query : {}", sql);
            try (final var rs = executeQuery(sql, pstmt, setter)) {
                if (rs.next()) {
                    return mapRow(rowMapper, rs, sql);
                }
                return null;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new ResultSetProcessingException(e, sql);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new ConnectionCloseException(e, sql);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new CannotGetJdbcConnectionException(e.getMessage(), e);
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Connection conn) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StatementPreparationException(e, sql);
        }
    }

    private ResultSet executeQuery(String sql, PreparedStatement pstmt, PreparedStatementSetter setter) {
        try {
            bindParams(sql, pstmt, setter);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new QueryExecutionException(e, sql);
        }
    }

    private int executeUpdate(String sql, PreparedStatement pstmt) {
        try {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UpdateExecutionException(e, sql);
        }
    }

    private <T> T mapRow(RowMapper<T> rowMapper, ResultSet rs, String sql) {
        try {
            return rowMapper.apply(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RowMappingException(e, sql);
        }
    }

    private void bindParams(String sql, PreparedStatement pstmt, PreparedStatementSetter setter) {
        try {
            setter.accept(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new ParameterBindingException(e, sql);
        }
    }

    private void bindParams(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
