package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, @Nullable Object... args) {
        final Executor<List<T>> executor = preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final RowMapperResultSetExtractor<T> rowMapperResultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
            return rowMapperResultSetExtractor.extractData(resultSet);
        };

        return executeQuery(sql, executor, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, @Nullable Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public int update(final String sql, @Nullable Object... args) {
        return executeQuery(sql, PreparedStatement::executeUpdate, args);
    }

    public int update(final Connection connection, final String sql, @Nullable Object... args) {
        return executeQuery(connection, sql, PreparedStatement::executeUpdate, args);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private <T> T executeQuery(final Connection connection, final String sql, final Executor<T> queryExecutor, final Object[] args) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            PreparedStatementSetter.setValues(preparedStatement, args);
            return queryExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T executeQuery(final String sql, final Executor<T> queryExecutor, final Object[] args) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            PreparedStatementSetter.setValues(preparedStatement, args);
            return queryExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
