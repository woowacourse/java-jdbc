package org.springframework.jdbc.core;

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

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... args) {
        preparedStatementExecutor.execute(
                getPreparedStatementCreator(sql, args),
                PreparedStatement::executeUpdate
        );
    }

    @Nullable
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = preparedStatementExecutor.execute(
                getPreparedStatementCreator(sql, args),
                preparedStatement -> mappingQueryResult(preparedStatement, rowMapper)
        );
        return results.isEmpty() ? null : results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return preparedStatementExecutor.execute(
                getPreparedStatementCreator(sql, args),
                preparedStatement -> mappingQueryResult(preparedStatement, rowMapper)
        );
    }

    private PreparedStatementCreator getPreparedStatementCreator(final String sql, final Object[] args) {
        return conn -> {
            log.debug("sql: {}", sql);
            final PreparedStatement preparedStatement = conn.prepareStatement(sql);
            setAllArguments(preparedStatement, args);
            return preparedStatement;
        };
    }

    private void setAllArguments(
            final PreparedStatement preparedStatement,
            final Object... args
    ) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> mappingQueryResult(
            final PreparedStatement preparedStatement,
            final RowMapper<T> rowMapper
    ) throws SQLException {
        final ResultSet resultSet = preparedStatement.executeQuery();
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            final T row = rowMapper.mapRow(resultSet);
            results.add(row);
        }
        resultSet.close();
        return results;
    }
}
