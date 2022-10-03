package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

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
        return query(sql, statement -> QueryExecutor.executeQuery(rowMapper, statement), params);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, statement -> QueryExecutor.executeQueryForList(rowMapper, statement), params);
    }

    private <T> T query(final String sql, final StatementCallBack<T> strategy, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setParams(statement, params);
            return strategy.apply(statement);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setParams(final PreparedStatement statement, final Object[] params) throws SQLException {
        int paramIndex = DEFAULT_PARAM_INDEX;
        for (final Object param : params) {
            statement.setObject(paramIndex++, param);
        }
    }
}
