package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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
    private static final String QUERY_RESULT_EXCEPTION_MESSAGE = "Incorrect result size: expected 1, actual ";
    private static final int MAXIMUM_QUERY_FOR_OBJECT_RESULT = 1;
    private static final int BASE_PARAMETER_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final PreparedStatementSetter pss) throws DataAccessException {
        final Connection conn = getConnection();
        final PreparedStatement pstmt = getPreparedStatement(conn, sql);

        try (conn; pstmt) {
            pss.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void update(final String sql, final Object... parameters) throws DataAccessException {
        update(sql, createPreparedStatementSetter(parameters));
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
            throw new DataAccessException("Get DB connection failed.");
        }
    }

    private PreparedStatement getPreparedStatement(final Connection conn, final String sql) {
        try {
            log.info("query = {}", sql);
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Create PrepareStatement failed.");
        }
    }

    private ResultSet getResultSet(final PreparedStatement pstmt, final PreparedStatementSetter pss) {
        try {
            pss.setValues(pstmt);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Get ResultSet failed.");
        }
    }
}
