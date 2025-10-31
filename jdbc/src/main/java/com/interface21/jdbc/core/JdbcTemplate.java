package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, args);
            final var result = pstmt.executeUpdate();
            log.debug("query : {}, result : {}", sql, result);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute update", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper, final Object... args) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, args);
            return mappingResultSet(pstmt, rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, final Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new DataAccessException("No data found");
        }
        if (results.size() > 1) {
            throw new DataAccessException("More than one result found");
        }
        return results.get(0);
    }

    private <T> List<T> mappingResultSet(PreparedStatement pstmt, RowMapper<T> rowMapper) {
        try (final var rs = pstmt.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to map ResultSet", e);
        }
    }

    private void setParameters(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
