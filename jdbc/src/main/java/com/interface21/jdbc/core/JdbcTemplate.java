package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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
    private static final String QUERY_RESULT_EXCEPTION_MESSAGE = "Incorrect result size: expected 1, actual ";
    private static final int MAXIMUM_QUERY_FOR_OBJECT_RESULT = 1;
    private static final int BASE_PARAMETER_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        execute(sql, createPreparedStatementSetter(parameters));
    }

    private void execute(final String sql, final PreparedStatementSetter pss) {
        final Connection conn = getConnection();
        final PreparedStatement pstmt = getPreparedStatement(conn, sql);

        try (conn; pstmt) {
            executePreparedStatement(pss, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void executePreparedStatement(final PreparedStatementSetter pss, final PreparedStatement pstmt)
            throws SQLException {
        pss.setValues(pstmt);
        pstmt.executeUpdate();
    }

    public void update(final Connection conn, final String sql, final Object... parameters) throws DataAccessException {
        executeWithConnection(conn, sql, createPreparedStatementSetter(parameters));
    }

    private void executeWithConnection(final Connection conn, final String sql, final PreparedStatementSetter pss) {
        final PreparedStatement pstmt = getPreparedStatement(conn, sql);

        try (pstmt) {
            executePreparedStatement(pss, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... parameters) {
        return pstmt -> {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + BASE_PARAMETER_INDEX, parameters[i]);
            }
        };
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        final Connection conn = getConnection();
        final PreparedStatement pstmt = getPreparedStatement(conn, sql);
        final ResultSet rs = getResultSet(pstmt, pss);

        try (conn; pstmt; rs) {
            return extractData(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, rowMapper, createPreparedStatementSetter(parameters));
    }

    private <T> List<T> extractData(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        return results;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return getSingleResult(results);
    }

    private <T> T getSingleResult(final List<T> results) {
        if (results.size() != MAXIMUM_QUERY_FOR_OBJECT_RESULT) {
            throw new DataAccessException(QUERY_RESULT_EXCEPTION_MESSAGE + results.size());
        }
        return results.getFirst();
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new CannotGetJdbcConnectionException("Get DB connection failed.");
        }
    }

    private PreparedStatement getPreparedStatement(final Connection conn, final String sql) {
        try {
            log.info("query = {}", sql);
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            closeConnection(conn);
            throw new DataAccessException("Create PrepareStatement failed.");
        }
    }

    private void closeConnection(final Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private ResultSet getResultSet(final PreparedStatement pstmt, final PreparedStatementSetter pss) {
        try {
            pss.setValues(pstmt);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            closePreparedStatement(pstmt);
            throw new DataAccessException("Get ResultSet failed.");
        }
    }

    private void closePreparedStatement(final PreparedStatement pstmt) {
        try {
            closeConnection(pstmt.getConnection());
            pstmt.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
