package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.ImpossibleSQLExecutionException;

public class JdbcConnector implements Connector {

    private final DataSource dataSource;

    public JdbcConnector(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T execute(final String sql, final QueryExecutor<T> queryExecutor, final Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            PreparedStatementStarter preparedStatementStarter = new SimplePreparedStatementStarter(preparedStatement);
            preparedStatementStarter.setParameters(parameters);

            return queryExecutor.executePreparedStatement(preparedStatementStarter);
        } catch (SQLException e) {
            throw new ImpossibleSQLExecutionException();
        }
    }
}
