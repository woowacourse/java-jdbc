package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import org.springframework.dao.SQLExceptionTranslator;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... parameters) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            setQueryParameters(pstmt, parameters);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            setQueryParameters(pstmt, parameters);
            try (ResultSet rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);

                T result = null;
                if (rs.next()) {
                    result = rowMapper.mapRow(rs, rs.getRow());
                }

                verifyResultRowSize(rs, 1);

                if (result == null) {
                    throw new SQLException("조회결과가업서!");
                }

                return result;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(e);
        }
    }

    private static void verifyResultRowSize(final ResultSet rs, final int rowSize) throws SQLException {
        rs.last();
        if (rowSize < rs.getRow()) {
            throw new SQLException(String.format("결과가 1개인 줄 알았는데, %d개 나왔서!", rs.getRow()));
            /**
             * 예외 원문 : Incorrect result size: expected 1, actual n
             */
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            setQueryParameters(pstmt, parameters);

            try (ResultSet rs = pstmt.executeQuery();) {
                log.debug("query : {}", sql);

                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rs.getRow()));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(e);
        }
    }

    private static void setQueryParameters(final PreparedStatement pstmt, final Object[] parameters) throws SQLException {
        for (int i = 1; i < parameters.length + 1; i++) {
            pstmt.setObject(i, parameters[i - 1]);
        }
    }
}
