package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.exception.JdbcTemplateException;

public class JdbcTemplate {

    private static final int START_STATEMENT_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int query(final String sql, final Object... statements) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = processPreparedStatement(connection, sql, statements)
        ) {
            return preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new JdbcTemplateException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... statements) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = processPreparedStatement(connection, sql, statements);
                final ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }

            return null;
        } catch (final SQLException e) {
            throw new JdbcTemplateException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... statements) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = processPreparedStatement(connection, sql, statements);
                final ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            final List<T> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }

            return result;
        } catch (final SQLException e) {
            throw new JdbcTemplateException(e);
        }
    }

    private PreparedStatement processPreparedStatement(
            final Connection connection,
            final String sql,
            final Object[] statements
    ) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);

        bindStatements(preparedStatement, statements);
        return preparedStatement;
    }

    private void bindStatements(
            final PreparedStatement preparedStatement,
            final Object[] statements
    ) throws SQLException {
        for (int i = START_STATEMENT_INDEX; i < statements.length + 1; i++) {
            preparedStatement.setObject(i, statements[i - START_STATEMENT_INDEX]);
        }
    }
}
