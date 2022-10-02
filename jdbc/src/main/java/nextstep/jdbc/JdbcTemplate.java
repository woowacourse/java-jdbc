package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

    public <T> List<T> executeQuery(String sql, ParameterSource parameterSource, RowMapper<T> rowMapper) {
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParams(preparedStatement, parameterSource);
            return query(preparedStatement, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> executeQueryForObject(String sql, ParameterSource parameterSource, RowMapper<T> rowMapper) {
        List<T> entities = executeQuery(sql, parameterSource, rowMapper);
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entities.get(0));
    }

    public void executeUpdate(String sql, ParameterSource parameterSource) {
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParams(preparedStatement, parameterSource);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(PreparedStatement preparedStatement, ParameterSource parameterSource) throws SQLException {
        for (var index = 0; index < parameterSource.getParamCount(); index++) {
            preparedStatement.setObject(index + 1, parameterSource.getParam(index));
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> query(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        try (final var resultSet = preparedStatement.executeQuery()) {
            List<T> entities = new ArrayList<>();
            while (resultSet.next()) {
                entities.add(rowMapper.mapRow(resultSet));
            }
            return entities;
        }
    }
}
