package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class JdbcConnector implements Connector {

    private final DataSource dataSource;

    public JdbcConnector(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T execute(String sql, QueryExecutor<T> queryExecutor, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i+1, parameters[i]);
            }
            return queryExecutor.executePreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
