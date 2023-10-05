package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final int LIMIT_SIZE_OF_DATA = 1;
    private static final int INDEX_OF_DATA = 0;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return getDataFromQuery(sql, rowMapper, args);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> result = getDataFromQuery(sql, rowMapper, args);

        validateSizeOfObjects(result);

        if (result.isEmpty()) {
            Optional.empty();
        }

        return Optional.of(result.get(INDEX_OF_DATA));
    }

    private <T> void validateSizeOfObjects(final List<T> result) {
        if (result.size() > LIMIT_SIZE_OF_DATA) {
            throw new DataAccessException("데이터의 사이즈가 1 초과");
        }
    }

    private <T> List<T> getDataFromQuery(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, preparedStatement -> getQueryResults(preparedStatement, rowMapper), args);
    }

    public void update(final String sql, final Object... args) {
        execute(sql, PreparedStatement::execute, args);
    }

    private <T> T execute(final String sql, final Executor<T> executor, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatement(preparedStatement, args);

            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getQueryResults(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapToRow(resultSet));
        }

        return result;
    }

    private void setPreparedStatement(final PreparedStatement preparedStatement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
