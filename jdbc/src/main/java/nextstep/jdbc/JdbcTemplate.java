package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final int DEFAULT_PARAM_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        query(sql, PreparedStatement::execute, params);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, statement -> QueryExecutor.executeQueryForObject(rowMapper, statement), params);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, statement -> QueryExecutor.executeQueryForList(rowMapper, statement), params);
    }

    private <T> T query(final String sql, final StatementCallBack<T> strategy, final Object... params) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            setParams(statement, params);
            return strategy.apply(statement);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParams(final PreparedStatement statement, final Object[] params) throws SQLException {
        int paramIndex = DEFAULT_PARAM_INDEX;
        for (final Object param : params) {
            statement.setObject(paramIndex++, param);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private static class QueryExecutor {
        private QueryExecutor() {
        }

        private static <T> T executeQueryForObject(final RowMapper<T> rowMapper, final PreparedStatement statement) {
            return getSingleResult(executeQuery(rowMapper, statement));
        }

        private static <T> List<T> executeQueryForList(final RowMapper<T> rowMapper,
                                                       final PreparedStatement statement) {
            return executeQuery(rowMapper, statement);
        }

        private static <T> List<T> executeQuery(final RowMapper<T> rowMapper, final PreparedStatement statement) {
            try (final ResultSet resultSet = statement.executeQuery()) {
                final List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(rowMapper.map(resultSet));
                }
                return result;
            } catch (final SQLException e) {
                throw new DataAccessException("query exception!", e);
            }
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
}
