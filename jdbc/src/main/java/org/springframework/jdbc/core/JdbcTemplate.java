package org.springframework.jdbc.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate extends JdbcTemplateBase {

    private static final Class<Integer> METHOD_FIRST_PARAMETER_TYPE = int.class;

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

    private void setParameters(final Object[] params, final PreparedStatement preparedStatement) {
        for (int i = 0; i < params.length; i++) {
            invokeSetMethod(preparedStatement, i + 1, params[i]);
        }
    }

    public void invokeSetMethod(final PreparedStatement preparedStatement,
                                final int order,
                                final Object parameter) {
        final SupportType type = SupportType.findType(parameter);
        final String methodName = type.getPreparedStatementMethodName();
        final Class<? extends PreparedStatement> aClass = preparedStatement.getClass();

        try {
            final Method method = aClass.getDeclaredMethod(methodName, METHOD_FIRST_PARAMETER_TYPE,
                type.getClassType());
            method.invoke(preparedStatement, order, parameter);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
