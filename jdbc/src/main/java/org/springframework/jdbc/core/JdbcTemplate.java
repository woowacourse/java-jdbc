package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final PrepareStatementGenerator PREPARED_STATEMENT_GENERATOR = (connection, sql) -> {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (final PreparedStatement preparedStatement = PREPARED_STATEMENT_GENERATOR.create(dataSource.getConnection(), sql)) {
            final PrepareStatementSetter psSetter = new ArgumentsPrepareStatementSetter(args);
            psSetter.setValue(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final PreparedStatement preparedStatement = PREPARED_STATEMENT_GENERATOR.create(dataSource.getConnection(), sql)) {
            final PrepareStatementSetter psSetter = new ArgumentsPrepareStatementSetter(args);
            psSetter.setValue(preparedStatement);
            List<T> results = extractData(rowMapper, preparedStatement);

            return results.iterator().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final PreparedStatement preparedStatement = PREPARED_STATEMENT_GENERATOR.create(dataSource.getConnection(), sql)) {

            return extractData(rowMapper, preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> extractData(final RowMapper<T> rowMapper, final PreparedStatement preparedStatement) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return results;
        }
    }
}
