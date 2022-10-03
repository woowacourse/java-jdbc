package nextstep.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

    public <T> List<T> query(final String sql, final Class<T> t, final Object... objects) {
        final var parsedSql = parseSql(sql, objects);
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(parsedSql);
             final ResultSet resultSet = statement.executeQuery()) {
            return query(t, resultSet);
        } catch (SQLException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String parseSql(final String sql, final Object... objects) {
        String parsedSql = sql;
        for (final Object object : objects) {
            if (object.getClass().isAssignableFrom(String.class)) {
                parsedSql = parsedSql.replaceFirst("\\?", "'" + object + "'");
                continue;
            }
            parsedSql = parsedSql.replaceFirst("\\?", String.valueOf(object));
        }
        return parsedSql;
    }

    private <T> List<T> query(final Class<T> t, final ResultSet resultSet)
            throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final List<T> queryResult = new ArrayList<>();
        while (resultSet.next()) {
            queryResult.add(createInstance(t, resultSet));
        }
        return queryResult;
    }

    private <T> T createInstance(final Class<T> t, final ResultSet resultSet)
            throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final var metaData = resultSet.getMetaData();
        final var columnCount = metaData.getColumnCount();
        final Object[] params = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            final String columnClassName = metaData.getColumnClassName(i + 1);
            final Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(columnClassName);
            params[i] = resultSet.getObject(i + 1, aClass);
        }
        final var constructor = findConstructor(t, metaData);
        return constructor.newInstance(params);
    }

    private <T> Constructor<T> findConstructor(final Class<T> t, final ResultSetMetaData metaData)
            throws SQLException, ClassNotFoundException, NoSuchMethodException {
        final var columnCount = metaData.getColumnCount();
        final Class[] classes = new Class[columnCount];
        for (int i = 0; i < columnCount; i++) {
            final String columnClassName = metaData.getColumnClassName(i + 1);
            final Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(columnClassName);
            classes[i] = aClass;
        }
        return t.getDeclaredConstructor(classes);
    }

    public <T> T queryForObject(final String sql, final Class<T> t, final Object... objects) {
        return query(sql, t, objects).get(0);
    }

    public void execute(final String sql, final Object... params) {
        final var parsedSql = parseSql(sql, params);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(parsedSql)
        ) {
            final var effectedRow = statement.executeUpdate();
            checkCompleted(effectedRow);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void checkCompleted(final int effectedRow) {
        if (effectedRow == 0) {
            throw new RuntimeException("쿼리가 정상적으로 적용되지 않았습니다.");
        }
    }
}
