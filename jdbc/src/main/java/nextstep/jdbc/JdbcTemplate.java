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
        query(sql, params, PreparedStatement::execute);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, params, statement -> QueryExecutor.getInstance()
                .executeQuery(rowMapper, statement));
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, params, statement -> QueryExecutor.getInstance()
                .executeQueryForList(rowMapper, statement));
    }

    private <T> T query(final String sql, final Object[] params, final StatementCallBack<T> strategy) {
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
