package nextstep.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.exception.DataAccessException;

public class ResultDataExtractor {

    public static <T> List<T> extractData(ResultSet resultSet, Class<T> clazz)  {
        final Constructor<T> constructor = getConstructor(clazz);

        List<T> objects = new ArrayList<>();
        try {
            while (resultSet.next()) {
                List<Object> parameters = extractData(resultSet, constructor);
                objects.add(constructor.newInstance(parameters.toArray()));
            }
        } catch (Exception exception){
            throw new DataAccessException();
        }

        return objects;
    }

    private static <T> Constructor<T> getConstructor(Class<T> clazz) {
        final Field[] declaredFields = clazz.getDeclaredFields();
        Class[] types = new Class[declaredFields.length];
        for (int i = 0; i < declaredFields.length; i++) {
            types[i] = declaredFields[i].getType();
        }
        try {
            return clazz.getDeclaredConstructor(types);
        } catch (NoSuchMethodException exception) {
            throw new DataAccessException();
        }
    }

    private static <T> List<Object> extractData(ResultSet resultSet, Constructor<T> constructor) {
        List<Object> parameters = new ArrayList<>();

        final int numOfParameterTypes = constructor.getParameterTypes().length;

        for (int i = 1; i <= numOfParameterTypes; i++) {
            parameters.add(bindData(resultSet, i));
        }
        return parameters;
    }

    private static Object bindData(ResultSet resultSet, int index) {
        try {
            return resultSet.getObject(index);
        } catch (SQLException exception){
            throw new DataAccessException();
        }
    }
}
