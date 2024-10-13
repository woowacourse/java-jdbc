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

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);

        if (isSingleResult(results)) {
            return Optional.empty();
        }

        return Optional.of(results.getFirst());
    }

    private <T> boolean isSingleResult(List<T> results) {
        return results.size() != 1;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = getPreparedStatement(connection, sql);
        ResultSet resultSet = execute(preparedStatement, args);

        try (connection; preparedStatement; resultSet) {
            return extractResults(rowMapper, resultSet);
        } catch (SQLException e) {
            log.error("EXECUTE_QUERY_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(sql + "을 실행하던 중 오류가 발생했습니다.");
        }
    }

    private ResultSet execute(PreparedStatement preparedStatement, Object... args) {
        try {
            PreparedStatementSetter.setValue(preparedStatement, args);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            log.info("EXECUTE_QUERY_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(preparedStatement + "을 실행하던 중 오류가 발생했습니다.");
        }
    }

    private <T> List<T> extractResults(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
        }
        return results;
    }

    public int update(String sql, Object... args) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = getPreparedStatement(connection, sql);

        try (connection; preparedStatement) {
            PreparedStatementSetter.setValue(preparedStatement, args);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.info("EXECUTE_UPDATE_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(sql + "을 실행하던 중 오류가 발생했습니다.");
        }
    }

    public int update(Connection connection, String sql, Object... args) {
        PreparedStatement preparedStatement = getPreparedStatement(connection, sql);

        try (preparedStatement) {
            PreparedStatementSetter.setValue(preparedStatement, args);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.info("EXECUTE_UPDATE_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException(sql + "을 실행하던 중 오류가 발생했습니다.");
        }
    }
}
