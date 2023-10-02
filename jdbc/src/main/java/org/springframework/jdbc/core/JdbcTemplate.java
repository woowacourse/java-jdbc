package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeException;
import org.springframework.dao.ResultEmptyException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (final Connection connection = DataSourceUtils.getConnection(dataSource);
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPreparedStatementArguments(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final Connection connection = DataSourceUtils.getConnection(dataSource);
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            ResultSet resultSet = pstmt.executeQuery();
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = DataSourceUtils.getConnection(dataSource);
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPreparedStatementArguments(pstmt, args);
            ResultSet resultSet = pstmt.executeQuery();
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }
            validateResultSize(results);
            return results.get(0);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatementArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int parameterIndex = 1; parameterIndex <= args.length; parameterIndex++) {
            pstmt.setObject(parameterIndex, args[parameterIndex - 1]);
        }
    }

    private <T> void validateResultSize(final List<T> results) {
        if (results.isEmpty()) {
            throw new ResultEmptyException("result is empty.");
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeException(
                    String.format("Incorrect result size : expected - %d, actual - %d", 1, results.size()));
        }
    }

}
