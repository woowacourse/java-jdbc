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

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public <T> List<T> query(final String sql, final Class<T> t) {
        try {
            final Connection connection = dataSource.getConnection();
            final PreparedStatement statement = connection.prepareStatement(sql);
            final ResultSet resultSet = statement.executeQuery();
            final List<T> queryResult = new ArrayList<>();
            while (resultSet.next()) {
                final ResultSetMetaData metaData = resultSet.getMetaData();
                final int columnCount = metaData.getColumnCount();
                final Class[] classes = new Class[columnCount];
                final Object[] params = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    final String columnClassName = metaData.getColumnClassName(i + 1);
                    final Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(columnClassName);
                    classes[i] = aClass;
                    params[i] = resultSet.getObject(i + 1, aClass);
                }
                final Constructor<?> constructor = t.getDeclaredConstructor(classes);
                queryResult.add((T) constructor.newInstance(params));
            }
            return queryResult;
        } catch (SQLException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
            InstantiationException | IllegalAccessException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
