package com.interface21.jdbc.core;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) throws CannotGetJdbcConnectionException {
        try (final var connection = dataSource.getConnection();
             final var pstmt = connection.prepareStatement(sql)) {

            for(var i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) throws CannotGetJdbcConnectionException {
        var results = query(sql, rowMapper, args);

        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new RuntimeException();
        }

        return results.getFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) throws CannotGetJdbcConnectionException {
        try (
            final var connection = dataSource.getConnection();
            final var pstmt = connection.prepareStatement(sql)
        ) {
            for(var i=0; i<args.length; i++) {
                pstmt.setObject(i+1, args[i]);
            }

            try(final var resultSet = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                var rows = 0;

                while(resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rows++));
                }

                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
