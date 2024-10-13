package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.dao.DataAccessException;

import static com.interface21.jdbc.core.ResultMapper.multipleResultMapping;
import static com.interface21.jdbc.core.ResultMapper.singleResultMapping;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T queryExecute(final Connection usedConnection, final String sql, final SqlExecutor<T> sqlExecutor) {
        if (Objects.nonNull(usedConnection)) {
            return queryExecuteOnConnection(usedConnection, sql, sqlExecutor);
        }
        try (Connection connection = dataSource.getConnection()) {
            return queryExecuteOnConnection(connection, sql, sqlExecutor);
        } catch (SQLException e) {
            log.error("DataSource로부터 Connection을 얻지 못했습니다. 예외 메세지: {}", e.getMessage(), e);
            throw new DataAccessException("데이터베이스 연결을 할 수 없습니다. 원인: " + e.getMessage(), e);
        }
    }

    private <T> T queryExecuteOnConnection(final Connection connection, final String sql, final SqlExecutor<T> sqlExecutor) {
        try (PreparedStatement preparedStatement = getPreparedStatement(sql, connection)) {
            return sqlExecutor.execute(preparedStatement, sql);
        } catch (SQLException e) {
            log.error("SQL 실행 중 오류가 발생했습니다. SQL: {}. 예외 메시지: {}", sql, e.getMessage(), e);
            throw new DataAccessException("SQL 실행 중 오류가 발생했습니다. SQL: " + sql + ". 원인: " + e.getMessage(), e);
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Connection connection) {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            log.error("SQL 쿼리에 대한 Statement을 준비하지 못했습니다. sql: {}. 예외 메세지: {}", sql, e.getMessage(), e);
            throw new DataAccessException("SQL 쿼리 준비 중 오류가 발생했습니다. SQL: " + sql + ". 원인: " + e.getMessage(), e);
        }
    }

    private <T> T queryResult(PreparedStatement preparedStatement, ResultSetMapper<T> resultMapper) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultMapper.apply(resultSet);
        }
    }

    public int update(Connection connection, String sql, Object[] args) {
        return queryExecute(connection, sql, (preparedStatement, query) -> {
            SqlParameterBinder.bind(preparedStatement, args);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper) {
        return queryExecute(connection, sql, (preparedStatement, query) ->
                queryResult(preparedStatement, resultSet -> multipleResultMapping(rowMapper, resultSet)));
    }

    public <T> List<T> query(Connection connection, String sql, Object[] args, RowMapper<T> rowMapper) {
        return queryExecute(connection, sql, (preparedStatement, query) -> {
            SqlParameterBinder.bind(preparedStatement, args);
            return queryResult(preparedStatement, resultSet -> multipleResultMapping(rowMapper, resultSet));
        });
    }

    public <T> T queryForObject(Connection connection, String sql, Object[] args, RowMapper<T> rowMapper) {
        return queryExecute(connection, sql, (preparedStatement, query) -> {
            SqlParameterBinder.bind(preparedStatement, args);
            return queryResult(preparedStatement, resultSet -> singleResultMapping(rowMapper, resultSet));
        });
    }
}
