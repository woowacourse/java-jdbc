package com.interface21.jdbc.core;


import com.interface21.dao.DataAccessException;
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

    public void update(String sql, Object... columns) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setColumn(columns, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            return retrieveData(rowMapper, preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... columns) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setColumn(columns, preparedStatement);
            List<T> records = retrieveData(rowMapper, preparedStatement);
            validateDataSize(records);
            return records.getFirst();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setColumn(Object[] columns, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 1; i <= columns.length; i++) {
            preparedStatement.setObject(i, columns[i - 1]);
        }
    }

    private <T> List<T> retrieveData(RowMapper<T> rowMapper, PreparedStatement preparedStatement) throws SQLException {
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
