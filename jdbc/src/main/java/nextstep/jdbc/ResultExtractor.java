package nextstep.jdbc;

import static java.lang.ClassLoader.getSystemClassLoader;

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

    public static <T> List<T> extractData(final Class<T> t, final ResultSet resultSet) {
        final List<T> results = new ArrayList<>();
        try {
            while (resultSet.next()) {
                results.add(InstanceCreator.createInstance(t, resultSet));
            }
            return results;
        } catch (SQLException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static class InstanceCreator {

        private InstanceCreator() {
            throw new RuntimeException("생성할 수 없는 클래스입니다.");
        }

        private static <T> T createInstance(final Class<T> t, final ResultSet resultSet)
                throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            final var metaData = resultSet.getMetaData();
            final var columnCount = metaData.getColumnCount();
            final var classes = new Class[columnCount];
            final var params = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                final var clazz = getClass(metaData, i);
                classes[i] = clazz;
                params[i] = getParam(resultSet, i, clazz);
            }
            return getConstructor(t, classes).newInstance(params);
        }

        private static Class<?> getClass(final ResultSetMetaData metaData, final int index)
                throws ClassNotFoundException, SQLException {
            final var columnClassName = metaData.getColumnClassName(index + 1);
            return getSystemClassLoader().loadClass(columnClassName);
        }

        private static Object getParam(final ResultSet resultSet, final int index,
                                       final Class<?> clazz) throws SQLException {
            return resultSet.getObject(index + 1, clazz);
        }

        private static <T> Constructor<T> getConstructor(final Class<T> t, final Class[] classes)
                throws NoSuchMethodException {
            final var constructor = t.getDeclaredConstructor(classes);
            constructor.setAccessible(true);
            return constructor;
        }
    }
}
