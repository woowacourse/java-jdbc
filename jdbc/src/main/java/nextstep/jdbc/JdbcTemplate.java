package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(PreparedStatement::executeUpdate, sql, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.getSingleResult(results);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(preparedStatement -> executeQueryAndMappingList(preparedStatement, rowMapper), sql, args);
    }

    private <T> List<T> executeQueryAndMappingList(final PreparedStatement preparedStatement,
                                                   final RowMapper<T> rowMapper) {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> results = new ArrayList<>();

            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private <T> T execute(final PreparedStatementCallback<T> preparedStatementCallback, final String sql,
                          final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = createStatement(connection, sql, args)) {
            return preparedStatementCallback.call(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private PreparedStatement createStatement(final Connection connection, final String sql, final Object[] args)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        int argumentCount = args.length;
        for (int i = 0; i < argumentCount; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }

        return preparedStatement;
    }
}
