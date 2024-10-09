package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T queryExecute(final String sql, final SqlExecutor<T> sqlExecutor) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(sql, connection)) {
            return sqlExecutor.execute(preparedStatement, sql);
        } catch (SQLException e) {
            log.error("SQL 실행 중 오류가 발생했습니다. SQL: {}. 예외 메시지: {}", sql, e.getMessage(), e);
            throw new DataAccessException("SQL 실행 중 오류가 발생했습니다. SQL: " + sql + ". 원인: " + e.getMessage(), e);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("DataSource로부터 Connection을 얻지 못했습니다. 예외 메세지: {}", e.getMessage(), e);
            throw new DataAccessException("데이터베이스 연결을 할 수 없습니다. 원인: " + e.getMessage(), e);
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

    public int update(String sql, Object[] args) {
        return queryExecute(sql, (preparedStatement, query) -> {
            SqlParameterBinder.bind(preparedStatement, args);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return queryExecute(sql, (preparedStatement, query) -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultMapping(rowMapper, resultSet);
            }
        });
    }

    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) {
        return queryExecute(sql, (preparedStatement, query) -> {
            SqlParameterBinder.bind(preparedStatement, args);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultMapping(rowMapper, resultSet);
            }
        });
    }

    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        return queryExecute(sql, (preparedStatement, query) -> {
            SqlParameterBinder.bind(preparedStatement, args);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return singleResultMapping(rowMapper, resultSet);
            }
        });
    }

    private <T> List<T> resultMapping(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private <T> T singleResultMapping(RowMapper<T> rowMapper, ResultSet resultSet) {
        try {
            return getSingResult(rowMapper, resultSet);
        } catch (SQLException e) {
            log.error("단일 결과 조회 중 SQL 예외가 발생했습니다. 예외 메시지: {}", e.getMessage(), e);
            throw new DataAccessException("단일 결과 조회 중 오류가 발생했습니다. 원인: " + e.getMessage(), e);
        }
    }

    private <T> T getSingResult(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            T result = rowMapper.mapRow(resultSet);
            validateSingleResult(resultSet);
            return result;
        }
        log.error("단일 행 조회를 기대했으나, 조회된 데이터가 없습니다.");
        throw new DataAccessException("단일 행 조회를 기대했지만, 조회된 행이 없습니다.");
    }

    private void validateSingleResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            log.error("단일 행 조회를 기대했으나, 여러 행이 조회되었습니다.");
            throw new DataAccessException("단일 행 조회를 기대했지만, 여러 행이 조회되었습니다.");
        }
    }
}
