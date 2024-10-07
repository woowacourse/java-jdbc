package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TypedPreparedStatementSetter implements PreparedStatementSetter {

    private static final int START_INDEX = 1;
    private static final ParameterSetter DEFAULT_PARAMETER_SETTER = PreparedStatement::setObject;
    private static final Map<Class<?>, ParameterSetter> PARAMETER_SETTERS = new HashMap<>();

    static {
        PARAMETER_SETTERS.put(Integer.class, (preparedStatement, index, parameter) -> preparedStatement.setInt(index, (Integer) parameter));
        PARAMETER_SETTERS.put(String.class, (preparedStatement, index, parameter) -> preparedStatement.setString(index, (String) parameter));
        PARAMETER_SETTERS.put(Boolean.class, (preparedStatement, index, parameter) -> preparedStatement.setBoolean(index, (Boolean) parameter));
        PARAMETER_SETTERS.put(Double.class, (preparedStatement, index, parameter) -> preparedStatement.setDouble(index, (Double) parameter));
        PARAMETER_SETTERS.put(Float.class, (preparedStatement, index, parameter) -> preparedStatement.setFloat(index, (Float) parameter));
        PARAMETER_SETTERS.put(Long.class, (preparedStatement, index, parameter) -> preparedStatement.setLong(index, (Long) parameter));
        PARAMETER_SETTERS.put(Short.class, (preparedStatement, index, parameter) -> preparedStatement.setShort(index, (Short) parameter));
        PARAMETER_SETTERS.put(Object.class, DEFAULT_PARAMETER_SETTER);
    }

    private final List<Object> parameters;

    public TypedPreparedStatementSetter(Object... parameters) {
        this(List.of(parameters));
    }

    public TypedPreparedStatementSetter(List<Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public void setParameters(PreparedStatement preparedStatement) throws SQLException {
        int preparedIndex = START_INDEX;
        for (Object parameter : parameters) {
            ParameterSetter parameterSetter = findParameterSetter(parameter);
            parameterSetter.set(preparedStatement, preparedIndex, parameter);
            preparedIndex++;
        }
    }

    private ParameterSetter findParameterSetter(Object parameter) {
        Class<?> valueType = parameter.getClass();
        ParameterSetter parameterSetter = PARAMETER_SETTERS.get(valueType);

        return Optional.ofNullable(parameterSetter)
                .orElse(DEFAULT_PARAMETER_SETTER);
    }

    @FunctionalInterface
    private interface ParameterSetter {
        void set(PreparedStatement preparedStatement, int index, Object parameter) throws SQLException;
    }
}
