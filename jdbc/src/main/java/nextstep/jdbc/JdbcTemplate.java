package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(final PreparedStatementGenerator generator,
                          final PreparedStatementExecuteStrategy<T> strategy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement statement = generator.generate(connection)) {
            return strategy.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void validateSql(final String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL은 null이거나 빈 값일 수 없습니다.");
        }
    }

    private void setParameters(final PreparedStatement statement, final Object... parameters)
            throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    public Long insert(final String sql, final Object... parameters) {
        validateSql(sql);
        return execute(
                connection -> connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS),
                preparedStatement -> {
                    setParameters(preparedStatement, parameters);
                    log.debug("query : {}", sql);
                    preparedStatement.executeUpdate();
                    return getGeneratedKey(preparedStatement);
                });
    }

    private Long getGeneratedKey(PreparedStatement preparedStatement) throws SQLException {
        final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        }
        return null;
    }

    public int update(final String sql, final Object... parameters) {
        validateSql(sql);
        return execute(
                connection -> connection.prepareStatement(sql),
                preparedStatement -> {
                    setParameters(preparedStatement, parameters);
                    log.debug("query : {}", sql);
                    return preparedStatement.executeUpdate();
                });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        validateSql(sql);
        return execute(
                connection -> connection.prepareStatement(sql),
                preparedStatement -> {
                    setParameters(preparedStatement, parameters);
                    log.debug("query : {}", sql);
                    final ResultSet resultSet = preparedStatement.executeQuery();
                    return mapResultSet(rowMapper, resultSet);
                });
    }

    private <T> List<T> mapResultSet(final RowMapper<T> rowMapper, final ResultSet resultSet)
            throws SQLException {
        final List<T> elements = new ArrayList<>();
        while (resultSet.next()) {
            elements.add(rowMapper.mapRow(resultSet, 0));
        }
        return elements;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters)
            throws DataAccessException {
        final List<T> results = query(sql, rowMapper, parameters);
        if (results.size() == 0) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.iterator().next();
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
