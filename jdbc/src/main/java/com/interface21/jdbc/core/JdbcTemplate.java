package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("GET_CONNECTION_EXCEPTION :: {}", e.getMessage(), e);
            throw new DataAccessException("Connection을 생성하던 중 오류가 발생했습니다.");
        }
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            log.error("GET_PREPARED_STATEMENT_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("PreparedStatement를 생성하던 중 오류가 발생했습니다.");
        }
    }

    private void close(Connection connection, PreparedStatement preparedStatement) {
        try {
            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            log.info("CLOSE_JDBC_RESOURCE_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("JDBC의 자원을 종료하던 중 오류가 발생했습니다.");
        }
    }

    private void setPreparedStatementParameter(Object[] args, PreparedStatement preparedStatement) {
        try {
            for (int paramterIndex = 1; paramterIndex <= args.length; paramterIndex++) {
                preparedStatement.setObject(paramterIndex, args[paramterIndex-1]);
            }
        } catch (SQLException e) {
            log.info("SET_PREPARED_STATEMENT_PARAMETER_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("PreparedStatement에 파라미터를 설정하던 중 오류가 발생했습니다.");
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = getPreparedStatement(connection, sql);
        try {
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }

            return results;
        } catch (SQLException e) {
            log.error("EXECUTE_QUERY_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(sql + "을 실행하던 중 오류가 발생했습니다.");
        } finally {
            close(connection, preparedStatement);
        }
    }

    public <T> Optional<T> queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = getPreparedStatement(connection, sql);

        try {
            setPreparedStatementParameter(args, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.info("EXECUTE_QUERY_FOR_OBJECT_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(sql + "을 실행하던 중 오류가 발생했습니다.");
        } finally {
            close(connection, preparedStatement);
        }
    }

    public int update(String sql, Object[] args) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = getPreparedStatement(connection, sql);

        try {
            setPreparedStatementParameter(args, preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.info("EXECUTE_UPDATE_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(sql + "을 실행하던 중 오류가 발생했습니다.");
        } finally {
            close(connection, preparedStatement);
        }
    }
}
