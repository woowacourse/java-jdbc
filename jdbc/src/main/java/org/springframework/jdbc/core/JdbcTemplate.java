package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private final PrepareStatementExecutor prepareStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.prepareStatementExecutor = new PrepareStatementExecutor(dataSource);
    }

    public void executeQuery(final String sql, final Object... params) {
        prepareStatementExecutor.execute(
                PreparedStatement::executeUpdate,
                sql,
                params
        );
    }

    public void executeQuery(final Connection connection, final String sql, final Object... params) {
        prepareStatementExecutor.execute(
                connection,
                PreparedStatement::executeUpdate,
                sql,
                params
        );
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final List<T> result = query(sql, rowMapper, params);
        if (result.size() != 1) {
            throw new DataAccessException("Result Count is Not Only 1. ResultCount=" + result.size());
        }
        return result.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return prepareStatementExecutor.execute(
                preparedStatement -> findQueryResults(rowMapper, preparedStatement),
                sql,
                params
        );
    }

    private <T> List<T> findQueryResults(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        ResultSet resultSet = pstmt.executeQuery();

        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }
}
