package nextstep.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public Long insert(final String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setSqlParameters(pstmt, args);
            pstmt.executeUpdate();
            return getGeneratedKey(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Long getGeneratedKey(PreparedStatement pstmt) {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<Object> finds(Class<?> classType, String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setSqlParameters(pstmt, args);
            Constructor<?> properConstructor = getProperConstructor(classType);

            List<Object> bindingObjects = new ArrayList<>();
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    bindingObjects.add(makeInstanceFromSqlResult(resultSet, properConstructor));
                }
            }
            return bindingObjects;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public Object find(Class<?> classType, String sql, Object...args) {
        List<Object> result = finds(classType, sql, args);
        if (result.size() != 1) {
            throw new IllegalStateException();
        }
        return result.get(0);
    }

    private Object makeInstanceFromSqlResult(ResultSet resultSet, Constructor<?> constructor) {
        int index = 1;
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        List<Object> constructorParameters = new ArrayList<>();
        for (Class<?> parameterType : parameterTypes) {
            constructorParameters.add(extractData(parameterType, resultSet, index++));
        }

        try {
            return constructor.newInstance(constructorParameters.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private Object extractData(Class<?> parameterType, ResultSet resultSet, int index) {
        try {
            if (String.class.equals(parameterType)) {
                return resultSet.getString(index);
            }
            if (Long.TYPE.equals(parameterType)) {
                return resultSet.getLong(index);
            }
            throw new IllegalStateException();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void setSqlParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            Class<?> aClass = arg.getClass();
            if (String.class.equals(aClass)) {
                pstmt.setString(index++, (String) arg);
            } else if (Long.class.equals(aClass) || Long.TYPE.equals(aClass)) {
                pstmt.setLong(index++, (long) arg);
            }
        }
    }

    private Constructor<?> getProperConstructor(Class<?> classType) {
        Constructor<?>[] constructors = classType.getConstructors();
        int fieldNumber = classType.getDeclaredFields().length;
        for (Constructor<?> constructor : constructors) {
            if (fieldNumber == constructor.getParameterCount()) {
                return constructor;
            }
        }
        throw new IllegalStateException();
    }
}
