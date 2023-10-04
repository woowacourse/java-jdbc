package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.exception.ResultSetOverflowException;

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

    public int update(final String sql, final Object... args) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setArguments(pstmt, args);

            log.debug("query : {}", sql);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            final int parameterIndex = i + 1;
            pstmt.setObject(parameterIndex, args[i]);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setArguments(pstmt, args);
            ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            return calculateResult(rowMapper, rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T calculateResult(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        if (rs.getRow() > 1) {
            throw new ResultSetOverflowException("결과가 한 개를 초과합니다.");
        }

        if (rs.next()) {
            return rowMapper.mapRow(rs, rs.getRow());
        }

        return null;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            return getResults(rowMapper, rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getResults(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();

        while (rs.next()) {
            final T result = calculateResult(rowMapper, rs);
            results.add(result);
        }

        return results;
    }
}
