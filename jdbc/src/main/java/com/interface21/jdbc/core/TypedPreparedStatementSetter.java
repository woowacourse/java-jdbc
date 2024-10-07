package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TypedPreparedStatementSetter implements PreparedStatementSetter {

    private static final int START_INDEX = 1;
    private static final ValueSetter DEFAULT_TYPE_SETTER = PreparedStatement::setObject;
    private static final Map<Class<?>, ValueSetter> valueSetters = new HashMap<>();

    static {
        valueSetters.put(Integer.class, (preparedStatement, index, value) -> preparedStatement.setInt(index, (Integer) value));
        valueSetters.put(String.class, (preparedStatement, index, value) -> preparedStatement.setString(index, (String) value));
        valueSetters.put(Boolean.class, (preparedStatement, index, value) -> preparedStatement.setBoolean(index, (Boolean) value));
        valueSetters.put(Double.class, (preparedStatement, index, value) -> preparedStatement.setDouble(index, (Double) value));
        valueSetters.put(Float.class, (preparedStatement, index, value) -> preparedStatement.setFloat(index, (Float) value));
        valueSetters.put(Long.class, (preparedStatement, index, value) -> preparedStatement.setLong(index, (Long) value));
        valueSetters.put(Short.class, (preparedStatement, index, value) -> preparedStatement.setShort(index, (Short) value));
        valueSetters.put(Object.class, DEFAULT_TYPE_SETTER);
    }

    private final List<Object> values;

    public TypedPreparedStatementSetter(Object... values) {
        this(List.of(values));
    }

    public TypedPreparedStatementSetter(List<Object> values) {
        this.values = values;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        int preparedIndex = START_INDEX;
        for (Object value : values) {
            ValueSetter valueSetter = findSqlTypeSetter(value);
            valueSetter.set(preparedStatement, preparedIndex, value);
            preparedIndex++;
        }
    }

    private ValueSetter findSqlTypeSetter(Object value) {
        Class<?> valueType = value.getClass();
        ValueSetter valueSetter = valueSetters.get(valueType);

        return Optional.ofNullable(valueSetter).orElse(DEFAULT_TYPE_SETTER);
    }

    @FunctionalInterface
    private interface ValueSetter {
        void set(PreparedStatement preparedStatement, int index, Object value) throws SQLException;
    }
}
