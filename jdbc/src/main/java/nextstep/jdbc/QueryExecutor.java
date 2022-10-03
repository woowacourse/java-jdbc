package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueryExecutor {

    private QueryExecutor() {
    }

    public static <T> T executeQuery(final RowMapper<T> rowMapper, final PreparedStatement statement) {
        return getSingleResult(execute(rowMapper, statement, QueryExecutor::extractResults));
    }

    public static <T> List<T> executeQueryForList(final RowMapper<T> rowMapper, final PreparedStatement statement) {
        return execute(rowMapper, statement, QueryExecutor::extractResults);
    }

    private static <T> List<T> execute(final RowMapper<T> rowMapper, final PreparedStatement statement,
                                       final ExtractStrategy<T> strategy) {
        try (final ResultSet resultSet = statement.executeQuery()) {
            return strategy.extract(rowMapper, resultSet);
        } catch (final SQLException e) {
            throw new DataAccessException("query exception!", e);
        }
    }

    private static <T> List<T> extractResults(final RowMapper<T> rowMapper, final ResultSet resultSet)
            throws SQLException {
        final List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.map(resultSet));
        }
        return result;
    }

    private static <T> T getSingleResult(final List<T> results) {
        if (results.size() > 1) {
            throw new DataAccessException("more than one result!");
        }

        if (results.isEmpty()) {
            throw new DataAccessException("query result is null");
        }

        return results.get(0);
    }
}
