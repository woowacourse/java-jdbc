package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate extends JdbcTemplateBase {

    public JdbcTemplate(final DataSource dataSource) {
        super(dataSource);
    }

    public void simpleExecute(final String sql) {
        executionBaseWithNonReturn(sql, PreparedStatement::execute);
    }

    public void simpleExecute(final String sql, Object... params) {
        executionBaseWithNonReturn(sql, preparedStatement -> {
            setParameters(params, preparedStatement);
            preparedStatement.execute();
        });
    }

    public <T> T executeQueryForObject(final String sql, final ResultSetObjectMapper<T> mapper) {
        return executionBaseWithReturn(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            return mapper.map(resultSet);
        });
    }

    public <T> T executeQueryForObject(final String sql,
                                       final ResultSetObjectMapper<T> mapper,
                                       final Object... params) {
        return executionBaseWithReturn(sql, preparedStatement -> {
            setParameters(params, preparedStatement);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return mapper.map(resultSet);
            }
            return null;
        });
    }

    public <T> List<T> executeQueryForObjects(final String sql, final ResultSetObjectMapper<T> mapper) {
        return executionBaseWithReturn(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<T> objects = new ArrayList<>();
            while (resultSet.next()) {
                objects.add(mapper.map(resultSet));
            }
            return objects;
        });
    }

    public <T> List<T> executeQueryForObjects(final String sql,
                                              final ResultSetObjectMapper<T> mapper,
                                              final Object... params) {
        return executionBaseWithReturn(sql, preparedStatement -> {
            setParameters(params, preparedStatement);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<T> objects = new ArrayList<>();
            while (resultSet.next()) {
                objects.add(mapper.map(resultSet));
            }
            return objects;
        });
    }

    private void setParameters(final Object[] params, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
}
