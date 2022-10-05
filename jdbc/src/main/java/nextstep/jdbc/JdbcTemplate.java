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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, params, rowMapper);
    }

    private <T> List<T> query(final String sql, final Object[] params, final RowMapper<T> rowMapper) {
        return execute(
                connection -> connection.prepareStatement(sql),
                preparedStatement -> makeQueryResult(rowMapper, preparedStatement, params)
        );
    }

    private <T> List<T> makeQueryResult(final RowMapper<T> rowMapper,
                                        final PreparedStatement preparedStatement,
                                        final Object[] params) throws SQLException {
        try (final ResultSet resultSet = getResultSet(preparedStatement, params)) {
            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }
            return result;
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final List<T> result = query(sql, params, rowMapper);
        validateSingleResultSize(result);
        return result.get(0);
    }

    private <T> void validateSingleResultSize(final List<T> result) {
        if (result.size() == 0) {
            throw new DataAccessException("쿼리 결과가 없음!");
        }
        if (result.size() > 1) {
            throw new DataAccessException("쿼리 결과가 넘 많음!");
        }
    }

    private ResultSet getResultSet(final PreparedStatement prepareStatement, final Object[] params)
            throws SQLException {
        setPreparedStatementParams(prepareStatement, params);

        return prepareStatement.executeQuery();
    }

    private void setPreparedStatementParams(final PreparedStatement preparedStatement, final Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    public Long insert(final String sql, Object... params) {
        return execute(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            setPreparedStatementParams(preparedStatement, params);
            return preparedStatement;
        }, new SimpleInsertCallback());
    }

    public Long insert(final PreparedStatementCreator preparedStatementCreator) {
        return execute(preparedStatementCreator, new SimpleInsertCallback());
    }

    public Integer update(final PreparedStatementCreator preparedStatementCreator) {
        return execute(preparedStatementCreator, new SimpleUpdateCallback());
    }

    public Integer update(final String sql, Object... params) {
        return execute(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            setPreparedStatementParams(preparedStatement, params);
            return preparedStatement;
        }, new SimpleUpdateCallback());
    }

    private <T> T execute(final PreparedStatementCreator preparedStatementCreator,
                          final PreparedStatementCallback<T> preparedStatementCallback) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection)) {
            return preparedStatementCallback.extract(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
