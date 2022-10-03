package nextstep.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultDataExtractor {

    public static <T> List<T> extractData(ResultSet resultSet, Class<T> clazz) throws Exception {
        final Constructor<T> constructor = getConstructor(clazz);

        List<T> objects = new ArrayList<>();
        while (resultSet.next()){
            List<Object> parameters = extractData(resultSet, constructor);
            objects.add(constructor.newInstance(parameters.toArray()));
        }

        return objects;
    }

    public static <T> T extractSingleData(ResultSet resultSet, Class<T> clazz) throws Exception {
        final Constructor<T> constructor = getConstructor(clazz);

        if (!resultSet.next()){
            throw new DataAccessException();
        }

        final List<Object> parameters = extractData(resultSet, constructor);
        return constructor.newInstance(parameters.toArray());
    }

    private static <T> Constructor<T> getConstructor(Class<T> clazz) {
        final Field[] declaredFields = clazz.getDeclaredFields();
        Class<T>[] types = new Class[declaredFields.length];
        for (int i = 0; i < declaredFields.length; i++) {
            types[i] = (Class<T>) declaredFields[i].getType();
        }
        try {
            return clazz.getDeclaredConstructor(types);
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException();
        }
    }

    private static <T> List<Object> extractData(ResultSet resultSet, Constructor<T> constructor) throws SQLException {
        List<Object> parameters = new ArrayList<>();

        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        int index = 1;

        for (Class<?> parameterType : parameterTypes) {
            parameters.add(bindData(resultSet, parameterType, index++));
        }

        return parameters;
    }

    private static Object bindData(ResultSet resultSet, Class<?> parameterType, int index) {
        try {
            if (parameterType == String.class) {
                return resultSet.getString(index);
            }

            if (parameterType == Long.class || parameterType == long.class) {
                return resultSet.getLong(index);
            }

            if (parameterType == Integer.class) {
                return resultSet.getInt(index);
            }
        } catch (SQLException exception){
            throw new RuntimeException();
        }
        throw new RuntimeException();
    }
}
