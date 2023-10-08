package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.UniqueResultException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Connection conn, final String sql, final Object... params) {
        returnResult(conn, sql, this::executeQuery, params);
    }

    private int executeQuery(final PreparedStatement pstmt) {
        try {
            final int executeResult = pstmt.executeUpdate();
            log.info("INSERT, UPDATE, DELETE 수행된 행 개수 = " + executeResult);
            return executeResult;
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public <T> T queryForObject(final Connection conn, final String sql, final RowMapper<T> mapper, Object... params) {
        final List<T> results = returnResult(conn, sql, pstmt -> queryForSelect(pstmt, mapper), params);

        if (results.isEmpty()) {
            throw new NoSuchElementException("데이터가 없습니다");
        }

        if (results.size() > 1) {
            throw new UniqueResultException("조회결과가 두개이상입니다. 개수 = " + results.size());
        }

        return results.get(0);
    }

    public <T> List<T> queryForObjects(final Connection conn, final String sql, final RowMapper<T> mapper, Object... params) {
        return returnResult(conn, sql, pstmt -> queryForSelect(pstmt, mapper), params);
    }

    private <T> List<T> queryForSelect(final PreparedStatement pstmt, final RowMapper<T> mapper) {
        try {
            final ResultSet rs = pstmt.executeQuery();
            final var queryResult = new ArrayList<T>();

            while (rs.next()) {
                queryResult.add(mapper.get(rs));
            }

            return queryResult;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T returnResult(final Connection conn, final String sql, final QueryLauncher<T> launcher, final Object... params) {
        try (PreparedStatement pstmt = makePreparedWhenHasParams(conn, sql, params)) {

            log.debug("query : {}", sql);

            return launcher.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement makePreparedWhenHasParams(final Connection conn, final String sql, final Object... params)
            throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);

        if (params.length < 1) {
            return pstmt;
        }

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt;
    }
}
