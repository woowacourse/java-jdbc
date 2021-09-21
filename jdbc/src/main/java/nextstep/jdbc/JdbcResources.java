package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.sql.DataSource;

public class JdbcResources {

    private final DataSource dataSource;
    private final Set<AutoCloseable> resources;

    public JdbcResources(final DataSource dataSource) {
        this(dataSource, new LinkedHashSet<>());
    }

    public JdbcResources(final DataSource dataSource, final Set<AutoCloseable> resources) {
        this.dataSource = dataSource;
        this.resources = resources;
    }

    public ResultSet getResultSet(String sql, final Object... arguments) throws SQLException {
        Connection connection = generateConnection();
        PreparedStatement preparedStatement = generatePreparedStatementBy(sql, connection);
        mapArgumentsToPreparedStatement(preparedStatement, arguments);

        ResultSet resultSet = preparedStatement.executeQuery();
        resources.add(resultSet);
        return resultSet;
    }

    private PreparedStatement generatePreparedStatementBy(final String sql, final Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        resources.add(preparedStatement);
        return preparedStatement;
    }

    private Connection generateConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        resources.add(connection);
        return connection;
    }

    private void mapArgumentsToPreparedStatement(final PreparedStatement preparedStatement, final Object[] arguments) throws SQLException {
        for (int argumentIndex = 1; argumentIndex <= arguments.length; argumentIndex++) {
            preparedStatement.setObject(argumentIndex, arguments[argumentIndex - 1]);
        }
    }

    public void closeAll() {
        for (AutoCloseable resource : resources) {
            close(resource);
        }
    }

    private void close(final AutoCloseable resource) {
        if (Objects.nonNull(resource)) {
            try {
                resource.close();
            } catch (Exception ignored) {
            }
        }
    }
}
