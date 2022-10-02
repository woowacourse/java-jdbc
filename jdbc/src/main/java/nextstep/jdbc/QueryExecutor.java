package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueryExecutor {

    private static final QueryExecutor INSTANCE;

    static {
        INSTANCE = new QueryExecutor();
    }

    private QueryExecutor() {
    }

    public static QueryExecutor getInstance() {
        return INSTANCE;
    }

    public <T> T executeQuery(final RowMapper<T> rowMapper, final PreparedStatement statement) {
        T result = null;
        try (final ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                result = rowMapper.map(resultSet);
            }
        } catch (final SQLException e) {
            throw new DataAccessException("query exception!", e);
        }
        return result;
    }

    public <T> List<T> executeQueryForList(final RowMapper<T> rowMapper, final PreparedStatement statement) {
        final List<T> result = new ArrayList<>();
        try (final ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(rowMapper.map(resultSet));
            }
        } catch (final SQLException e) {
            throw new DataAccessException("query exception!", e);
        }
        return result;
    }

}
