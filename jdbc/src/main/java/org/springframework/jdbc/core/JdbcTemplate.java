package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.exception.InvalidDataSizeException;

public class JdbcTemplate {

    private static final int FIRST_INDEX_OF_RESULT = 0;
    private static final int MIN_RESULT_SIZE = 1;

    private final QueryExecutorService queryExecutorService;

    public JdbcTemplate(final DataSource dataSource) {
        this.queryExecutorService = new QueryExecutorService(dataSource);
    }

    public void update(final Connection connection, final String query, final Object... columns) {
        queryExecutorService.execute(connection, PreparedStatement::executeUpdate, query, columns);
    }

    public <T> List<T> query(final Connection connection, final String query, final RowMapper<T> rowMapper,
                             final Object... columns) {
        return execute(connection, query, rowMapper, columns);
    }

    public <T> Optional<T> queryForObject(final Connection connection, final String query, final RowMapper<T> rowMapper,
                                          final Object... columns) {
        final List<T> results = execute(connection, query, rowMapper, columns);
        validateSize(results);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(FIRST_INDEX_OF_RESULT));
    }

    private <T> List<T> execute(final Connection connection, final String query, final RowMapper<T> rowMapper,
                                final Object[] columns) {
        return queryExecutorService.execute(
                connection,
                pstmt -> {
                    final ResultSet resultSet = pstmt.executeQuery();
                    return getResult(rowMapper, resultSet);
                },
                query,
                columns
        );
    }

    private <T> List<T> getResult(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.map(rs));
        }
        return result;
    }

    private <T> void validateSize(final List<T> result) {
        if (result.size() > MIN_RESULT_SIZE) {
            throw new InvalidDataSizeException("결과가 1건 이상 조회되었습니다.");
        }
    }
}
