package nextstep.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultExtractor {

    private static final Logger log = LoggerFactory.getLogger(ResultExtractor.class);

    private ResultExtractor() {
        throw new RuntimeException("생성할 수 없는 클래스입니다.");
    }

    public static <T> List<T> extractData(final Class<T> targetType, final ResultSet resultSet) {
        final List<T> results = new ArrayList<>();
        try {
            while (resultSet.next()) {
                results.add(InstanceCreator.createInstance(targetType, resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static class InstanceCreator {

        private InstanceCreator() {
            throw new RuntimeException("생성할 수 없는 클래스입니다.");
        }

        private static <T> T createInstance(final Class<T> targetType, final ResultSet resultSet)
                throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            final var metaData = resultSet.getMetaData();
            final var columnCount = metaData.getColumnCount();
            final var classes = new Class[columnCount];
            final var params = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                classes[i] = getClass(metaData, i);
                params[i] = getParam(resultSet, i);
            }
            return getConstructor(targetType, classes).newInstance(params);
        }

        private static Class<?> getClass(final ResultSetMetaData metaData, final int index)
                throws SQLException, ClassNotFoundException {
            final var className = metaData.getColumnClassName(index + 1);
            return ClassLoader.getSystemClassLoader().loadClass(className);
        }

        private static Object getParam(final ResultSet resultSet, final int index) throws SQLException {
            return resultSet.getObject(index + 1);
        }

        private static <T> Constructor<T> getConstructor(final Class<T> targetType, final Class<?>[] classes)
                throws NoSuchMethodException {
            final var constructor = targetType.getDeclaredConstructor(classes);
            constructor.setAccessible(true);
            return constructor;
        }
    }
}
