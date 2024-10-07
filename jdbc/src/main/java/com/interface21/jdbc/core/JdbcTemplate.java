package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int PARAMETER_START_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("GET_CONNECTION_ERROR :: {}", e.getMessage(), e);
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

    private void setPreparedStatementParameter(Object[] args, PreparedStatement preparedStatement) {
        try {
            for (int parameterIndex = PARAMETER_START_INDEX; parameterIndex <= args.length; parameterIndex++) {
                preparedStatement.setObject(parameterIndex, args[parameterIndex - PARAMETER_START_INDEX]);
            }
        } catch (SQLException e) {
            log.info("SET_PREPARED_STATEMENT_PARAMETER_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("PreparedStatement에 파라미터를 설정하던 중 오류가 발생했습니다.");
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(connection, sql);
             ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }

            return results;
        } catch (SQLException e) {
            log.error("EXECUTE_QUERY_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(sql + "을 실행하던 중 오류가 발생했습니다.");
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object ...args) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(connection, sql)
        ) {
            setPreparedStatementParameter(args, preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(rowMapper.mapRow(resultSet, resultSet.getRow()));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            log.info("EXECUTE_QUERY_FOR_OBJECT_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(sql + "을 실행하던 중 오류가 발생했습니다.");
        }
    }

    public int update(String sql, Object ...args) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(connection, sql)
        ){
            setPreparedStatementParameter(args, preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.info("EXECUTE_UPDATE_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(sql + "을 실행하던 중 오류가 발생했습니다.");
        }
    }
}
