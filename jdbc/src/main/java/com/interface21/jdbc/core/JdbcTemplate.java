package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            final ResultSet resultSet = executeQuery(args, pstmt);

            return newRowMapperRsExtractor(rowMapper).extractData(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException("query for multiple entity failed (sql=" + sql + ")", e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            final ResultSet resultSet = executeQuery(args, pstmt);
            final List<T> mapped = newRowMapperRsExtractor(rowMapper).extractData(resultSet);

            return DataAccessUtils.singleResult(mapped);
        } catch (SQLException e) {
            throw new DataAccessException("query for single entity failed (sql=" + sql + ")", e);
        }
    }

    private ResultSet executeQuery(final Object[] args, final PreparedStatement pstmt) throws SQLException {
        newArgPreparedStatementSetter(args).setValues(pstmt);
        return pstmt.executeQuery();
    }

    public void update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            newArgPreparedStatementSetter(args).setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("update/insert failed (sql=" + sql + ")", e);
        }
    }

    public void updateWithConnection(final Connection connection, final String sql, final Object... args) {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            newArgPreparedStatementSetter(args).setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("update/insert failed (sql=" + sql + ")", e);
        }
    }

    private PreparedStatementSetter newArgPreparedStatementSetter(final Object[] args) {
        return new ArgumentPreparedStatementSetter(args);
    }

    private <T> RowMapperResultSetExtractor<T> newRowMapperRsExtractor(final RowMapper<T> rowMapper) {
        return new RowMapperResultSetExtractor<>(rowMapper);
    }
}
