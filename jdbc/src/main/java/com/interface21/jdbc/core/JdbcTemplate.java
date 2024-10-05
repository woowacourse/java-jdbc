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
    public static final String QUERY_RESULT_EXCEPTION_MESSAGE = "Incorrect result size: expected 1, actual ";
    public static final int EMPTY_COUNT = 0;
    public static final int MAXIMUM_QUERY_FOR_OBJECT_RESULT = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setPreparedStatement(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatement(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = EMPTY_COUNT; i < args.length; i++) {
            pstmt.setObject(i + MAXIMUM_QUERY_FOR_OBJECT_RESULT, args[i]);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);

            return extractData(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            setPreparedStatement(pstmt, args);
            rs = pstmt.executeQuery();

            return extractData(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
        }
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
        if (results.isEmpty()) {
            throw new DataAccessException(QUERY_RESULT_EXCEPTION_MESSAGE + EMPTY_COUNT);
        }
        if (results.size() > MAXIMUM_QUERY_FOR_OBJECT_RESULT) {
            throw new DataAccessException(QUERY_RESULT_EXCEPTION_MESSAGE + results.size());
        }
        return results.getFirst();
    }

    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
