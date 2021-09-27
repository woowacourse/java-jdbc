package nextstep.jdbc.templates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import nextstep.jdbc.utils.ConnectionUtils;
import nextstep.jdbc.utils.preparestatement.PreparedStatementCallback;
import nextstep.jdbc.utils.preparestatement.PreparedStatementCreator;
import nextstep.jdbc.utils.statement.StatementCallback;

public abstract class BaseJdbcTemplate {

    private final DataSource dataSource;

    public BaseJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected <T> T execute(StatementCallback<T> action) {
        final Connection connection;
        try {
            connection = ConnectionUtils.getConnection(dataSource);
            try (Statement stmt = connection.createStatement()) {

                final T result = action.getResult(stmt);
                if (ConnectionUtils.isTransactionStarted()) {
                    return result;
                }
                ConnectionUtils.closeConnection();
                return result;
            }
        } catch (SQLException sqlException) {
            throw new JdbcException();
        }
    }

    protected <T> T execute(PreparedStatementCreator preparedStatementCreator,
                            PreparedStatementCallback<T> action) {
        final Connection connection;
        try {
            connection = ConnectionUtils.getConnection(dataSource);
            try (PreparedStatement preparedStatement =
                preparedStatementCreator.createPreparedStatement(connection)) {

                final T result = action.getResult(preparedStatement);
                if (ConnectionUtils.isTransactionStarted()) {
                    return result;
                }
                ConnectionUtils.closeConnection();
                return result;
            }
        } catch (SQLException sqlException) {
            throw new JdbcException();
        }
    }
}
