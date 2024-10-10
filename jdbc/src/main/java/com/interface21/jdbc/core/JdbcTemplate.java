package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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
    private static final int PREPARED_STATEMENT_INDEX_OFFSET = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        execute(sql,
                PreparedStatement::executeUpdate,
                args
        );
    }

    public void update(final Connection connection, String sql, final Object... args) {
        execute(
                connection,
                sql,
                PreparedStatement::executeUpdate,
                args
        );
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> result = query(sql, rowMapper, args);
        if (result.isEmpty()) {
            return null;
        }
        return result.getFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql,
                preparedStatement -> executeQueryAndMap(rowMapper, preparedStatement),
                args
        );
    }

    private <T> List<T> executeQueryAndMap(RowMapper<T> rowMapper, PreparedStatement preparedStatement)
            throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        return mapResultSet(rowMapper, resultSet);
    }

    private <T> List<T> mapResultSet(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }

    private <T> T execute(final String sql, final PreparedStatementExecutor<T> executor, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setStatement(args, statement);
            log.info("query : {}", sql);
            return executor.execute(statement);
        } catch (Exception e) {
            throw new DataAccessException("SQL execution failed. : " + sql + " \nCause: " + e.getMessage());
        }
    }

    private <T> T execute(
            final Connection connection,
            final String sql,
            final PreparedStatementExecutor<T> executor,
            final Object... args
    ) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setStatement(args, statement);
            log.info("query : {}", sql);
            return executor.execute(statement);
        } catch (Exception e) {
            throw new DataAccessException("SQL execution failed. : " + sql + " \nCause: " + e.getMessage());
        }
    }

    private void setStatement(Object[] args, PreparedStatement statement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + PREPARED_STATEMENT_INDEX_OFFSET, args[i]);
        }
    }
}
