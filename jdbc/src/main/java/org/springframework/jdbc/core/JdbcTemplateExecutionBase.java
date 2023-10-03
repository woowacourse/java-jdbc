package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.mapper.ResultSetObjectMapper;

public class JdbcTemplateExecutionBase extends JdbcTemplateBase {

    public JdbcTemplateExecutionBase(final DataSource dataSource) {
        super(dataSource);
    }

    protected <T> T executeQueryForObjectBase(final String sql,
                                              final ResultSetObjectMapper<T> mapper,
                                              final Object[] params,
                                              final boolean isTransactionEnable) {
        final JdbcTemplateExecutor<T> execution = preparedStatement -> {
            setParameters(params, preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapper.map(resultSet);
                }
                return null;
            }
        };
        return super.executionBaseWithReturn(sql, execution, isTransactionEnable);
    }

    protected <T> List<T> executeQueryForObjectsBase(final String sql,
                                                     final ResultSetObjectMapper<T> mapper,
                                                     final Object[] params,
                                                     final boolean isTransactionEnable) {
        final JdbcTemplateExecutor<List<T>> execution = preparedStatement -> {
            setParameters(params, preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<T> objects = new ArrayList<>();
                while (resultSet.next()) {
                    objects.add(mapper.map(resultSet));
                }
                return objects;
            }
        };
        return super.executionBaseWithReturn(sql, execution, isTransactionEnable);
    }

    protected void updateBase(final String sql,
                              final Object[] params,
                              final boolean isTransactionEnable) {
        final JdbcTemplateVoidExecution execution = preparedStatement -> {
            setParameters(params, preparedStatement);
            preparedStatement.executeUpdate();
        };
        super.executionBaseWithNonReturn(sql, execution, isTransactionEnable);
    }

    private void setParameters(final Object[] params, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
}
