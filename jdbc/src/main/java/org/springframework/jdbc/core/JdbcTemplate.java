package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParametersInPreparedStatement(preparedStatement, parameters);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParametersInPreparedStatement(
            PreparedStatement preparedStatement,
            Object[] parameters
    ) throws SQLException {
        for (int parameterNumber = 0; parameterNumber < parameters.length; parameterNumber++) {
            preparedStatement.setString(parameterNumber + 1, String.valueOf(parameters[parameterNumber]));
        }
    }

    public <T> List<T> queryForList(
            String sql, 
            RowMapper rowMapper,
            Class<T> clazz,
            Object... parameters
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParametersInPreparedStatement(preparedStatement, parameters);
            return getQueryResults(clazz, rowMapper, preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> getQueryResults(
            Class<T> clazz,
            RowMapper rowMapper,
            PreparedStatement preparedStatement
    ) throws SQLException {
        List<T> queryResults = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            queryResults.add(
                    clazz.cast(rowMapper.mapRow(resultSet))
            );
        }

        return queryResults;
    }

    public <T> T queryForObject(
            String sql,
            RowMapper rowMapper,
            Class<T> clazz,
            Object... parameters
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParametersInPreparedStatement(preparedStatement, parameters);
            return getQueryResult(clazz, rowMapper, preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T getQueryResult(
            Class<T> clazz,
            RowMapper rowMapper,
            PreparedStatement preparedStatement
    ) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        T queryResult = null;
        int rowCount = 0;

        while (resultSet.next()) {
            rowCount++;
            queryResult = clazz.cast(rowMapper.mapRow(resultSet));
        }

        validateResultSetSize(rowCount);
        return queryResult;
    }

    private void validateResultSetSize(int rowCount) {
        if (rowCount != 1) {
            throw new DataAccessException("조회 결과가 올바르지 않습니다.");
        }
    }

}
