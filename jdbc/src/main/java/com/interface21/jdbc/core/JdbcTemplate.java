package com.interface21.jdbc.core;


import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.EmptyResultDataAccessException;
import com.interface21.jdbc.exception.IncorrectResultSizeDataAccessException;
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
    private static final int QUERY_FOR_OBJECT_EXPECTED_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {
        log.debug("query : {}", sql);
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            executeUpdate(preparedStatement, preparedStatementSetter);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void executeUpdate(PreparedStatement preparedStatement, PreparedStatementSetter preparedStatementSetter)
            throws SQLException {
        preparedStatementSetter.setColumns(preparedStatement);
        preparedStatement.executeUpdate();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        log.debug("query : {}", sql);
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return executeQuery(rowMapper, preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        log.debug("query : {}", sql);
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setColumns(preparedStatement);
            List<T> records = executeQuery(rowMapper, preparedStatement);
            validateDataSize(records);
            return records.getFirst();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> executeQuery(RowMapper<T> rowMapper, PreparedStatement preparedStatement)
            throws SQLException {
        List<T> result = new ArrayList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                result.add(rowMapper.map(resultSet));
            }
        }
        return result;
    }

    private <T> void validateDataSize(List<T> result) {
        if (result.isEmpty()) {
            throw new EmptyResultDataAccessException();
        }
        if (result.size() != QUERY_FOR_OBJECT_EXPECTED_SIZE) {
            throw new IncorrectResultSizeDataAccessException(QUERY_FOR_OBJECT_EXPECTED_SIZE, result.size());
        }
    }
}
