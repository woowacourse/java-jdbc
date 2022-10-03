package nextstep.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, Class<T> clazz, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {


            setParameters(pstmt, args);
            final ResultSet resultSet = pstmt.executeQuery();
            final Constructor<T> constructor = getConstructor(clazz);

            List<T> objects = new ArrayList<>();
            while (resultSet.next()){
                List<Object> parameters = extractData(resultSet, constructor);
                objects.add(constructor.newInstance(parameters.toArray()));
            }

            return objects;
        } catch (Exception exception) {
            throw new RuntimeException();
        }
    }

    public <T> T queryForObject(String sql, Class<T> clazz, Object... args) {
        final List<T> objects = query(sql, clazz, args);
        return objects.get(0);
    }

    private void setParameters(PreparedStatement pstmt, Object[] objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            final Class<?> clazz = objects[i].getClass();
            if(clazz == String.class) {
                pstmt.setString(i+1, (String) objects[i]);
            }

            if(clazz == Long.class || clazz == long.class) {
                pstmt.setLong(i+1, (long) objects[i]);
            }
        }
    }

    private <T> Constructor<T> getConstructor(Class<T> clazz) {
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

    private <T> List<Object> extractData(ResultSet resultSet, Constructor<T> constructor) throws SQLException {
        List<Object> parameters = new ArrayList<>();

        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        int index = 1;

        for (Class<?> parameterType : parameterTypes) {
            parameters.add(bindData(resultSet, parameterType, index++));
        }

        return parameters;
    }

    private Object bindData(ResultSet resultSet, Class<?> parameterType, int index) {
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

    public void update(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            setParameters(pstmt, args);

            pstmt.executeUpdate();
        } catch (Exception exception) {
            throw new DataAccessException();
        }
    }
}
