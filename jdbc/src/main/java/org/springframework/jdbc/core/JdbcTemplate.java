package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.exception.InvalidDataSizeException;

public class JdbcTemplate {

    private static final int FIRST_INDEX_OF_RESULT = 0;
    private static final int MIN_RESULT_SIZE = 1;

    private final QueryExecutorService queryExecutorService;

    public JdbcTemplate(final DataSource dataSource) {
        this.queryExecutorService = new QueryExecutorService(dataSource);
    }

    public void update(final String query, final Object... columns) {
        queryExecutorService.execute(PreparedStatement::executeUpdate, query, columns);
    }

    public <T> List<T> query(final String query, final RowMapper<T> rowMapper, final Object... columns) {
        return queryExecutorService.execute(pstmt -> {
            final ResultSet resultSet = pstmt.executeQuery();
            final List<T> result = getResult(rowMapper, resultSet);
            closeResultSet(resultSet);
            return result;
        }, query, columns);
    }

    public <T> Optional<T> queryForObject(final String query, final RowMapper<T> rowMapper, final Object... columns) {
        final List<T> results = queryExecutorService.execute(pstmt -> {
            final ResultSet resultSet = pstmt.executeQuery();
            final List<T> result = getResult(rowMapper, resultSet);
            closeResultSet(resultSet);
            return result;
        }, query, columns);

        validateSize(results);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(FIRST_INDEX_OF_RESULT));
    }

    private <T> List<T> getResult(final RowMapper<T> rowMapper, final ResultSet rs) {
        final List<T> result = new ArrayList<>();
        try {
            while (rs.next()) {
                result.add(rowMapper.map(rs));
            }
            return result;
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> void validateSize(final List<T> result) {
        if (result.size() > MIN_RESULT_SIZE) {
            throw new InvalidDataSizeException("결과가 1건 이상 조회되었습니다.");
        }
    }

    private void closeResultSet(final ResultSet resultSet) {
        try {
            resultSet.close();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
