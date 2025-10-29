package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotCloseJdbcConnectionException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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
    private static final PositionalParameterPreparedStatementSetter DEFAULT_PREPARED_STATEMENT_SETTER = new PositionalParameterPreparedStatementSetter();

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * parameters를 SQL의 물음표 마커(?) 순서대로 바인딩 후 SQL를 실행합니다.
     *
     * @param sql        실행할 SQL
     * @param parameters SQL의 물음표 마커(?)에 바인딩될 파라미터들
     */
    public int update(final String sql, final Object... parameters) throws DataAccessException {
        return update(sql, DEFAULT_PREPARED_STATEMENT_SETTER.getPreparedStatementSetter(parameters));
    }

    /**
     * preparedStatementSetter로 바인딩 후 SQL를 실행합니다.
     *
     * @param sql                     실행할 SQL
     * @param preparedStatementSetter 파라미터를 바인딩할 PreparedStatementSetter 구현체
     */
    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter)
            throws DataAccessException {
        return execute(sql, preparedStatementSetter,
                preparedStatement -> executeUpdate(preparedStatement));
    }

    private int executeUpdate(final PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeUpdate();
    }

    /**
     * parameters를 SQL의 물음표 마커(?) 순서대로 바인딩 후 SQL 쿼리를 실행합니다. SQL 쿼리의 단일 결과를 RowMapper로 매핑하여 반환합니다.
     *
     * @param sql        실행할 SQL 쿼리
     * @param rowMapper  결과 행(Row)을 매핑하는 RowMapper 구현체
     * @param parameters SQL의 물음표 마커(?)에 바인딩될 파라미터들
     * @param <T>        매핑된 결과 객체의 타입
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters)
            throws DataAccessException {
        return queryForObject(sql, rowMapper, DEFAULT_PREPARED_STATEMENT_SETTER.getPreparedStatementSetter(parameters));
    }

    /**
     * preparedStatementSetter로 바인딩 후 SQL 쿼리를 실행합니다. SQL 쿼리의 단일 결과를 RowMapper로 매핑하여 반환합니다.
     *
     * @param sql                     실행할 SQL
     * @param rowMapper               결과 행(Row)을 매핑하는 RowMapper 구현체
     * @param preparedStatementSetter 파라미터를 바인딩할 PreparedStatementSetter 구현체
     * @param <T>                     매핑된 결과 객체의 타입
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                                final PreparedStatementSetter preparedStatementSetter) throws DataAccessException {
        return execute(sql, preparedStatementSetter,
                preparedStatement -> executeQueryAndMapSingleResult(preparedStatement, rowMapper));
    }

    private <T> T executeQueryAndMapSingleResult(final PreparedStatement preparedStatement,
                                                 final RowMapper<T> rowMapper) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            return mapSingleResult(resultSet, rowMapper);
        }
    }

    private <T> T mapSingleResult(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = mapResults(resultSet, rowMapper);
        if (results.isEmpty()) {
            throw new DataAccessException("Not found result");
        }
        if (results.size() > 1) {
            throw new DataAccessException("Multiple results");
        }
        return results.getFirst();
    }

    /**
     * parameters를 SQL의 물음표 마커(?) 순서대로 바인딩 후 SQL 쿼리를 실행합니다. SQL 쿼리의 결과를 RowMapper로 매핑하여 List로 반환합니다.
     *
     * @param sql        실행할 SQL 쿼리
     * @param rowMapper  결과 행(Row)을 매핑하는 RowMapper 구현체
     * @param parameters SQL의 물음표 마커(?)에 바인딩될 파라미터들
     * @param <T>        매핑된 결과 객체의 타입
     */
    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters)
            throws DataAccessException {
        return query(sql, rowMapper, DEFAULT_PREPARED_STATEMENT_SETTER.getPreparedStatementSetter(parameters));
    }

    /**
     * preparedStatementSetter로 바인딩 후 SQL 쿼리를 실행합니다. SQL 쿼리의 결과를 RowMapper로 매핑하여 List로 반환합니다.
     *
     * @param sql                     실행할 SQL
     * @param rowMapper               결과 행(Row)을 매핑하는 RowMapper 구현체
     * @param preparedStatementSetter 파라미터를 바인딩할 PreparedStatementSetter 구현체
     * @param <T>                     매핑된 결과 객체의 타입
     */
    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                             final PreparedStatementSetter preparedStatementSetter) throws DataAccessException {
        return execute(sql, preparedStatementSetter,
                preparedStatement -> executeQueryAndMapResults(preparedStatement, rowMapper));
    }

    private <T> List<T> executeQueryAndMapResults(final PreparedStatement preparedStatement,
                                                  final RowMapper<T> rowMapper) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            return mapResults(resultSet, rowMapper);
        }
    }

    private <T> List<T> mapResults(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        int rowNum = 1;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final PreparedStatementCallback<T> preparedStatementCallback
    ) throws DataAccessException, CannotGetJdbcConnectionException, CannotCloseJdbcConnectionException {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return doExecuteWithConnection(connection, sql, preparedStatementSetter, preparedStatementCallback);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private <T> T doExecuteWithConnection(
            final Connection connection,
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final PreparedStatementCallback<T> preparedStatementCallback
    ) throws DataAccessException {
        log.debug("query : {}", sql);
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return executePreparedStatement(preparedStatement, preparedStatementSetter, preparedStatementCallback);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executePreparedStatement(
            final PreparedStatement preparedStatement,
            final PreparedStatementSetter preparedStatementSetter,
            final PreparedStatementCallback<T> preparedStatementCallback
    ) throws SQLException {
        preparedStatementSetter.setParameters(preparedStatement);
        return preparedStatementCallback.doInPreparedStatement(preparedStatement);
    }
}
