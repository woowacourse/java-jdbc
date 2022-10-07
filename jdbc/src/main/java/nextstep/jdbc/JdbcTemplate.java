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
import org.springframework.lang.Nullable;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, @Nullable final Object... args) {
        final ResultSetMapper<List<T>> resultSetMapper = resultSetMapperQuery(rowMapper);
        final PreparedStatementExecutor<List<T>> preparedStatementExecutor = (preparedStatement) ->
                mapToResult(preparedStatement.executeQuery(), resultSetMapper);
        return execute(sql, preparedStatementExecutor, prepareStatementSetter(args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, @Nullable final Object... args) {
        final PreparedStatementExecutor<T> preparedStatementExecutor = (preparedStatement) ->
                mapToResult(preparedStatement.executeQuery(), resultSetMapperQueryForObject(rowMapper));
        return execute(sql, preparedStatementExecutor, prepareStatementSetter(args));
    }

    public int update(final String sql, @Nullable final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, prepareStatementSetter(args));
    }

    private PrepareStatementSetter prepareStatementSetter(final Object[] args) {
        return (preparedStatement) -> {
            for (int i = 1; i <= Objects.requireNonNull(args).length; i++) {
                preparedStatement.setObject(i, args[i - 1]);
            }
        };
    }

    private <T> ResultSetMapper<List<T>> resultSetMapperQuery(final RowMapper<T> rowMapper) {
        return (rs) -> {
            final List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                rowNum += 1;
                final T row = rowMapper.mapToRow(rs, rowNum);
                results.add(row);
            }
            return results;
        };
    }

    private <T> ResultSetMapper<T> resultSetMapperQueryForObject(final RowMapper<T> rowMapper) {
        return (rs) -> {
            final List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                rowNum += 1;
                final T row = rowMapper.mapToRow(rs, rowNum);
                results.add(row);
            }
            if (results.size() > 1) {
                throw new DataAccessException("1개보다 많은 값이 존재합니다.");
            }
            return results.get(0);
        };
    }

    private <T> T execute(final String sql, final PreparedStatementExecutor<T> preparedStatementExecutor,
                          final PrepareStatementSetter prepareStatementSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            prepareStatementSetter.setParams(preparedStatement);
            log.debug("query : {}", sql);
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
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
}
