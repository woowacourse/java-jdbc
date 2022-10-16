package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.lang.Nullable;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, @Nullable final Object... args) {
        final ResultSetMapper<List<T>> resultSetMapper = (rs) -> resultSetMapperQuery(rs, rowMapper);
        final PreparedStatementExecutor<List<T>> preparedStatementExecutor = (preparedStatement) ->
                mapToResult(preparedStatement.executeQuery(), resultSetMapper);
        final PrepareStatementSetter prepareStatementSetter = (preparedStatement) -> prepareStatementSetter(
                preparedStatement, args);
        return execute(sql, preparedStatementExecutor, prepareStatementSetter);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, @Nullable final Object... args) {
        final ResultSetMapper<T> resultSetMapper = (rs) -> resultSetMapperQueryForObject(rs, rowMapper);
        final PreparedStatementExecutor<T> preparedStatementExecutor = (preparedStatement) ->
                mapToResult(preparedStatement.executeQuery(), resultSetMapper);
        final PrepareStatementSetter prepareStatementSetter = (preparedStatement) -> prepareStatementSetter(
                preparedStatement, args);
        return execute(sql, preparedStatementExecutor, prepareStatementSetter);
    }

    public int update(final String sql, @Nullable final Object... args) {
        final PrepareStatementSetter prepareStatementSetter = (preparedStatement) -> prepareStatementSetter(
                preparedStatement, args);
        return execute(sql, PreparedStatement::executeUpdate, prepareStatementSetter);
    }

    private void prepareStatementSetter(final PreparedStatement preparedStatement, final Object[] args)
            throws SQLException {
        final Object[] arguments = Objects.requireNonNull(args);
        for (int i = 1; i <= arguments.length; i++) {
            preparedStatement.setObject(i, args[i - 1]);
        }
    }

    private <T> List<T> resultSetMapperQuery(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (rs.next()) {
            final T row = rowMapper.mapToRow(rs);
            results.add(row);
        }
        return results;
    }

    private <T> T resultSetMapperQueryForObject(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (rs.next()) {
            final T row = rowMapper.mapToRow(rs);
            results.add(row);
        }
        if (results.isEmpty()) {
            throw new DataAccessException("해당 데이터가 존재하지 않습니다.");
        }
        if (results.size() > 1) {
            throw new DataAccessException("1개보다 많은 값이 존재합니다.");
        }
        return results.get(0);
    }

    private <T> T execute(final String sql, final PreparedStatementExecutor<T> preparedStatementExecutor,
                          final PrepareStatementSetter prepareStatementSetter) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            prepareStatementSetter.setParams(preparedStatement);
            log.debug("query : {}", sql);
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private <T> T mapToResult(final ResultSet resultSet, final ResultSetMapper<T> resultSetMapper) {
        try (resultSet) {
            return resultSetMapper.mapToResult(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
