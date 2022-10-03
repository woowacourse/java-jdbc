package nextstep.jdbc;

import nextstep.jdbc.exception.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public enum TypeExecutor {

    INTEGER(Integer.class, TypeExecutor::setInt),
    LONG(Long.class, TypeExecutor::setLong),
    STRING(String.class, TypeExecutor::setString),
    DEFAULT(Object.class, TypeExecutor::setObject);

    private final Class type;

    private final TriConsumer<PreparedStatement, Integer, Object> consumer;
    TypeExecutor(final Class type, final TriConsumer<PreparedStatement, Integer, Object> consumer) {
        this.type = type;
        this.consumer = consumer;
    }

    public static void execute(final PreparedStatement preparedStatement, final int index, final Object value) {
        final TypeExecutor typeExecutor = Arrays.stream(values())
                .filter(it -> it.type.equals(value.getClass()))
                .findFirst()
                .orElseGet(() -> DEFAULT);
        typeExecutor.consumer
                .apply(preparedStatement, index, value);
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
