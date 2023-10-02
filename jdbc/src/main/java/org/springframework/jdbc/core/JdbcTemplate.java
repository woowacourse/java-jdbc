package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... args) {
        preparedStatementExecutor.execute(conn -> {
            final PreparedStatement preparedStatement = conn.prepareStatement(sql);
            setAllArguments(preparedStatement, args);
            return preparedStatement;
        });
    }

    @Nullable
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        log.debug("query : {}", sql);
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setAllArguments(preparedStatement, args);
            final ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                final T result = rowMapper.mapRow(resultSet);
                resultSet.close();
                return result;
            }

            resultSet.close();
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        log.debug("query : {}", sql);
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setAllArguments(preparedStatement, args);
            final ResultSet resultSet = preparedStatement.executeQuery();

            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                final T result = rowMapper.mapRow(resultSet);
                results.add(result);
            }

            resultSet.close();
            return results;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setAllArguments(
            final PreparedStatement preparedStatement,
            final Object... args
    ) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
