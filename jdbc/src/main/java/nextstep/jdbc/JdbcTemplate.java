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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParams(preparedStatement, args);
            return executeQuery(preparedStatement, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> entities = query(sql, rowMapper, args);
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entities.get(0));
    }

    public void update(String sql, Object... args) {
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParams(preparedStatement, args);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(PreparedStatement preparedStatement, Object... args) throws SQLException {
        for (var index = 0; index < args.length; index++) {
            preparedStatement.setObject(index + 1, args[index]);
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

    private <T> List<T> executeQuery(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        try (final var resultSet = preparedStatement.executeQuery()) {
            List<T> entities = new ArrayList<>();
            while (resultSet.next()) {
                entities.add(rowMapper.mapRow(resultSet));
            }
            return entities;
        }
    }
}
