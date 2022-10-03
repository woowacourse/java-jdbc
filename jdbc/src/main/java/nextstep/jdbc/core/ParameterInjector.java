package nextstep.jdbc.core;

import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.support.TriConsumer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public enum ParameterInjector {

    INTEGER(Integer.class, ParameterInjector::setInt),
    LONG(Long.class, ParameterInjector::setLong),
    STRING(String.class, ParameterInjector::setString),
    DEFAULT(Object.class, ParameterInjector::setObject);

    private final Class type;
    private final TriConsumer<PreparedStatement, Integer, Object> consumer;

    ParameterInjector(final Class type, final TriConsumer<PreparedStatement, Integer, Object> consumer) {
        this.type = type;
        this.consumer = consumer;
    }

    public static void inject(final PreparedStatement preparedStatement, final int index, final Object value) {
        final ParameterInjector parameterInjector = Arrays.stream(values())
                .filter(it -> it.type.equals(value.getClass()))
                .findFirst()
                .orElseGet(() -> DEFAULT);
        parameterInjector.consumer
                .consume(preparedStatement, index, value);
    }

    private static void setInt(final PreparedStatement preparedStatement, final int index, final Object value) {
        try {
            preparedStatement.setInt(index, (Integer) value);
        } catch (SQLException e) {
            throw new DataAccessException("파라미터를 int로 지정할 수 없습니다.");
        }
    }

    private static void setLong(final PreparedStatement preparedStatement, final int index, final Object value) {
        try {
            preparedStatement.setLong(index, (Long) value);
        } catch (SQLException e) {
            throw new DataAccessException("파라미터를 long으로 지정할 수 없습니다.");
        }
    }

    private static void setString(final PreparedStatement preparedStatement, final int index, final Object value) {
        try {
            preparedStatement.setString(index, (String) value);
        } catch (SQLException e) {
            throw new DataAccessException("파라미터를 String으로 지정할 수 없습니다.");
        }
    }

    private static void setObject(final PreparedStatement preparedStatement, final int index, final Object value) {
        try {
            preparedStatement.setObject(index, value);
        } catch (SQLException e) {
            throw new DataAccessException("파라미터를 Object로 지정할 수 없습니다.");
        }
    }
}
