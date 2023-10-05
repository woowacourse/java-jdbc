package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private static final int ONE_DATA_INDEX = 0;
    private static final int TWO = 2;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String query, final Object... args) {
        return execute(this::executeUpdate, query, args);
    }

    private int executeUpdate(final PreparedStatement pstmt) {
        try {
            return pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final Function<PreparedStatement, T> function, final String query, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = getPreparedStatement(conn, query, args)) {
            return function.apply(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> queryForObject(final String query, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = executeQuery(query, rowMapper, args);

        if (results.size() >= TWO) {
            throw new DataAccessException("2개 이상의 데이터가 존재합니다");
        }

        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(ONE_DATA_INDEX));
    }

    public <T> List<T> queryForList(final String query, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(query, rowMapper, args);
    }

    private <T> List<T> executeQuery(final String query, final RowMapper<T> rowMapper, final Object... args) {
        return execute(pstmt -> {
            try {
                final ResultSet rs = pstmt.executeQuery();
                final List<T> objects = new ArrayList<>();
                while (rs.next()) {
                    objects.add(rowMapper.mapToRow(rs));
                }
                return objects;
            } catch (final SQLException e) {
                throw new DataAccessException("Error execute query", e);
            }
        }, query, args);
    }

    private PreparedStatement getPreparedStatement(final Connection connection, final String sql, final Object... args)
            throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }

        return preparedStatement;
    }
}
