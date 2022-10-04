package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(sql, args, rowMapper);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> result = executeQuery(sql, args, rowMapper);
        if (result.size() > 1) {
            throw new DataAccessException("Incorrect result size: expected " + 1 + ", actual " + result.size());
        }
        return result.iterator().next();
    }

    public void update(final String sql, final Object... args) {
        validateIsNull(sql, "SQL must not be null");
        log.debug("execute SQL update [{}]", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, args);
            statement.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> executeQuery(final String sql, final Object[] args, final RowMapper<T> rowMapper) {
        validateIsNull(sql, "SQL must not be null");
        log.debug("execute SQL query [{}]", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql);
             final ResultSet resultSet = statement.executeQuery()) {
            setValues(statement, args);
            return extractData(resultSet, rowMapper);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setValues(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> extractData(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        validateIsNull(rowMapper, "RowMapper is required");
        if (resultSet == null) {
            return Collections.emptyList();
        }

        final List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }

    private void validateIsNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
