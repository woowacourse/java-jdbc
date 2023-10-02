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
import org.springframework.dao.EmptyDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

public class JdbcTemplate {
    // TODO: 2023-10-02 모든 Exception은 UnChekcedException으로 바꾸기

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);

        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        if (results.isEmpty()) {
            throw new EmptyDataAccessException();
        }

        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParamsToPreparedStatement(pstmt, args);

            final ResultSet resultSet = pstmt.executeQuery();

            return mapMultipleResults(resultSet, rowMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setParamsToPreparedStatement(final PreparedStatement pstmt, final Object[] args)
        throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> mapMultipleResults(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
        }

        return results;
    }

    public int update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParamsToPreparedStatement(pstmt, args);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
