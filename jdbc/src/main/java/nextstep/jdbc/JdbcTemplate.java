package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int FIRST_INDEX = 0;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final PreparedStatementExecutor<List<T>> callback = preparedStatement -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        };

        return execute(sql, callback, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> query = query(sql, rowMapper, args);
        return query.get(FIRST_INDEX);
    }

    private <T> T execute(final String sql, final PreparedStatementExecutor<T> callback, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(preparedStatement, args);
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error("error : {}", e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object... args)
            throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            statement.setObject(i, args[i - 1]);
        }
    }
}
