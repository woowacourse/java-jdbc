package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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
     * 파라미터 마커 나열 순서대로 parameters를 매핑하여 SQL 업데이트를 실행합니다
     *
     * @param sql        실행할 SQL 업데이트 문
     * @param parameters SQL 업데이트에 사용할 파라미터들
     */
    public void update(final String sql, final Object... parameters) {
        update(sql, DEFAULT_PREPARED_STATEMENT_SETTER.getPreparedStatementSetter(parameters));
    }

    public void update(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        execute(
                sql,
                preparedStatementSetter,
                preparedStatement -> executeUpdate(preparedStatement)
        );
    }

    private <T> T executeUpdate(
            final PreparedStatement preparedStatement
    ) throws SQLException {
        preparedStatement.executeUpdate();
        return null;
    }

    /**
     * 파라미터 마커 나열 순서대로 parameters를 매핑하여 SQL 쿼리를 실행하고, 단일 결과를 RowMapper로 매핑하여 반환합니다
     *
     * @param sql        실행할 SQL 업데이트 문
     * @param rowMapper  결과 행을 매핑하는 RowMapper
     * @param parameters SQL 쿼리에 사용할 파라미터들
     * @param <T>        매핑된 결과 객체의 타입
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return queryForObject(
                sql,
                DEFAULT_PREPARED_STATEMENT_SETTER.getPreparedStatementSetter(parameters),
                rowMapper
        );
    }

    public <T> T queryForObject(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final RowMapper<T> rowMapper
    ) {
        return execute(
                sql,
                preparedStatementSetter,
                preparedStatement -> executeQueryAndMapSingleResult(preparedStatement, rowMapper)
        );
    }

    private <T> T executeQueryAndMapSingleResult(
            final PreparedStatement preparedStatement,
            final RowMapper<T> rowMapper
    ) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            return mapSingleResult(resultSet, rowMapper);
        }
    }

    private <T> T mapSingleResult(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = mapResults(resultSet, rowMapper);
        if (results.isEmpty()) {
            throw new DataAccessException("Not found result");
        }
        return results.getFirst();
    }

    /**
     * 파라미터 마커 나열 순서대로 parameters를 매핑하여 SQL 쿼리를 실행하고, 결과를 RowMapper로 매핑하여 리스트로 반환합니다
     *
     * @param sql        실행할 SQL 업데이트 문
     * @param rowMapper  결과 행을 매핑하는 RowMapper
     * @param parameters SQL 쿼리에 사용할 파라미터들
     * @param <T>        매핑된 결과 객체의 타입
     */
    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(
                sql,
                DEFAULT_PREPARED_STATEMENT_SETTER.getPreparedStatementSetter(parameters),
                rowMapper
        );
    }

    public <T> List<T> query(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final RowMapper<T> rowMapper
    ) {
        return execute(
                sql,
                preparedStatementSetter,
                preparedStatement -> executeQueryAndMapResults(preparedStatement, rowMapper)
        );
    }

    private <T> List<T> executeQueryAndMapResults(
            final PreparedStatement preparedStatement,
            final RowMapper<T> rowMapper
    ) throws SQLException {
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
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            return doExecute(preparedStatement, preparedStatementSetter, preparedStatementCallback, sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T doExecute(
            final PreparedStatement preparedStatement,
            final PreparedStatementSetter preparedStatementSetter,
            final PreparedStatementCallback<T> preparedStatementCallback,
            final String sql
    ) throws SQLException {
        preparedStatementSetter.setParameters(preparedStatement);
        log.debug("query : {}", sql);
        return preparedStatementCallback.doInPreparedStatement(preparedStatement);
    }
}
