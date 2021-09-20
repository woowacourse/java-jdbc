package nextstep.jdbc.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ObjectConverter {

    public static <T> T convertSingleObject(ResultSet rs, Class<T> type) throws SQLException {
        try {
            return convertWithNoArgConstructor(rs, type);
        } catch (NoSuchMethodException e) {
            return convertWithMultiArgsConstructor(rs, type);
        } catch (Exception e) {
            throw new SQLException();
        }
    }

    private static <T> T convertWithNoArgConstructor(ResultSet rs, Class<T> type)
        throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException, SQLException {
        T instance = type.getConstructor().newInstance();
        for (Field declaredField : type.getDeclaredFields()) {
            declaredField.setAccessible(true);
            declaredField
                .set(instance, rs.getObject(declaredField.getName(), declaredField.getType()));
        }
        return instance;
    }

    private static <T> T convertWithMultiArgsConstructor(ResultSet rs, Class<T> type)
        throws SQLException {
        final Constructor<?> constructor =
            Arrays.stream(type.getConstructors())
                .min((o1, o2) -> o2.getParameterCount() - o1.getParameterCount())
                .orElseThrow(() -> new IllegalStateException("no match constructor exception"));
        Object[] constructorParameters = new Object[constructor.getParameterCount()];
        final Parameter[] parameters = constructor.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            constructorParameters[i] = rs.getObject(i + 1);
        }
        try {
            return (T) constructor.newInstance(constructorParameters);
        } catch (Exception instantiationException) {
            throw new SQLException();
        }
    }
}
