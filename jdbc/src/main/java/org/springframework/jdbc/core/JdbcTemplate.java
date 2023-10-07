package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.JdbcException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import javax.xml.crypto.OctetStreamData;
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
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (
                final PreparedStatement pstmt = getPrepareStatement(conn, sql, args)
        ) {
            log.debug("run sql {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (
                final PreparedStatement pstmt = getPrepareStatement(conn, sql, args);
                final ResultSet resultSet = pstmt.executeQuery()
        ) {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
            log.debug("run sql {}", sql);
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (
                final PreparedStatement pstmt = getPrepareStatement(conn, sql, args);
                final ResultSet resultSet = pstmt.executeQuery()
        ) {
            log.debug("run sql {}", sql);
            if (resultSet.next()) {
                final T result = rowMapper.map(resultSet);
                validateIsOnlyResult(resultSet);
                return result;
            }
            logAndThrowNoDataFoundException();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException(e);
        }
        return null;
    }

    private void validateIsOnlyResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            log.error("selected data count is larger than 1");
            throw new JdbcException("selected data count is larger than 1");
        }
    }

    private static void logAndThrowNoDataFoundException() {
        log.error("no data found");
        throw new JdbcException("no data found");
    }

    private PreparedStatement getPrepareStatement(final Connection connection, final String sql, final Object[] args) throws SQLException {
        final PreparedStatement pstm = connection.prepareStatement(sql);
        setSqlParameters(pstm, args);
        return pstm;
    }

    private void setSqlParameters(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int parameterIndex = 0; parameterIndex < args.length; parameterIndex++) {
            pstmt.setObject(parameterIndex + 1, args[parameterIndex]);
        }
    }
}
