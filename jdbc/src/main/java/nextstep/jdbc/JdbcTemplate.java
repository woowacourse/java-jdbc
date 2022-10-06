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
import java.util.stream.Collectors;
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

    public void update(final String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setSqlParameters(pstmt, args);
            pstmt.executeUpdate();
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
            return makeObjectsFromSql(pstmt, classType);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private List<Object> makeObjectsFromSql(PreparedStatement pstmt, Class<?> classType) {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            Constructor<?> constructor = getProperConstructor(classType);
            return bindingObjects(resultSet, constructor);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private List<Object> bindingObjects(ResultSet resultSet, Constructor<?> constructor) throws SQLException {
        List<Object> objects = new ArrayList<>();
        while (resultSet.next()) {
            objects.add(bindingObject(resultSet, constructor));
        }
        return objects;
    }


    private Object bindingObject(ResultSet resultSet, Constructor<?> constructor) {
        try {
            List<Object> constructorParameters = extractDatas(resultSet,
                    constructor.getParameterTypes());
            return constructor.newInstance(constructorParameters.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private List<Object> extractDatas(ResultSet resultSet, Class<?>[] dataTypes) {
        int index = 1;
        List<Object> datas = new ArrayList<>();
        for (Class<?> dataType : dataTypes) {
            datas.add(extractData(dataType, resultSet, index++));
        }
        return datas;
    }

    private Object extractData(Class<?> dataType, ResultSet resultSet, int index) {
        try {
            if (String.class.equals(dataType)) {
                return resultSet.getString(index);
            }
            if (Long.TYPE.equals(dataType)) {
                return resultSet.getLong(index);
            }
            throw new IllegalStateException();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public Object find(Class<?> classType, String sql, Object... args) {
        List<Object> result = finds(classType, sql, args);
        if (result.size() != 1) {
            throw new IllegalStateException();
        }
        return result.get(0);
    }

    private void setSqlParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
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

    public void deleteAll(final String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
