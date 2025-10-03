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
    private static final DefaultPreparedStatementSetters DEFAULT_PREPARED_STATEMENT_SETTERS = new DefaultPreparedStatementSetters();

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 파라미터 마커 나열 순서대로 parameters를 매핑하는 PreparedStatementSetter를 사용하여 SQL 업데이트를 실행합니다
     *
     * @param sql        실행할 SQL 업데이트 문
     * @param parameters SQL 업데이트에 사용할 파라미터들
     */
    public void update(final String sql, final Object... parameters) {
        update(sql, DEFAULT_PREPARED_STATEMENT_SETTERS.getPreparedStatementSetter(parameters), parameters);
    }

    public void update(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final Object... parameters
    ) {
        execute(
                sql,
                preparedStatementSetter,
                preparedStatement -> {
                    preparedStatement.executeUpdate();
                    return null;
                },
                parameters
        );
    }

    /**
     * 파라미터 마커 나열 순서대로 parameters를 매핑하는 PreparedStatementSetter를 사용하여 SQL 쿼리를 실행하고, 단일 결과를 RowMapper로 매핑하여 반환합니다
     *
     * @param sql        실행할 SQL 업데이트 문
     * @param rowMapper  결과 행을 매핑하는 RowMapper
     * @param parameters SQL 쿼리에 사용할 파라미터들
     * @param <T>        매핑된 결과 객체의 타입
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return queryForObject(
                sql,
                DEFAULT_PREPARED_STATEMENT_SETTERS.getPreparedStatementSetter(parameters),
                rowMapper,
                parameters
        );
    }

    public <T> T queryForObject(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final RowMapper<T> rowMapper,
            final Object... parameters
    ) {
        return execute(
                sql,
                preparedStatementSetter,
                preparedStatement -> {
                    try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                        return mapSingleResult(resultSet, rowMapper);
                    }
                },
                parameters
        );
    }

    private <T> T mapSingleResult(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        if (resultSet.next()) {
            return rowMapper.mapRow(resultSet, 1);
        }
        throw new SQLException("No data found");
    }

    /**
     * 파라미터 마커 나열 순서대로 parameters를 매핑하는 PreparedStatementSetter를 사용하여 SQL 쿼리를 실행하고, 결과를 RowMapper로 매핑하여 리스트로 반환합니다
     *
     * @param sql        실행할 SQL 업데이트 문
     * @param rowMapper  결과 행을 매핑하는 RowMapper
     * @param parameters SQL 쿼리에 사용할 파라미터들
     * @param <T>        매핑된 결과 객체의 타입
     */
    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(
                sql,
                DEFAULT_PREPARED_STATEMENT_SETTERS.getPreparedStatementSetter(parameters),
                rowMapper,
                parameters
        );
    }

    public <T> List<T> query(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final RowMapper<T> rowMapper,
            final Object... parameters
    ) {
        return execute(
                sql,
                preparedStatementSetter,
                preparedStatement -> {
                    try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                        return mapResult(resultSet, rowMapper);
                    }
                },
                parameters
        );
    }

    private <T> List<T> mapResult(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
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
            final PreparedStatementCallback<T> preparedStatementCallback,
            final Object... parameters
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatementSetter.setParameters(preparedStatement);
            log.debug("query : {}", sql);

            return preparedStatementCallback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
