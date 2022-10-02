package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractPreparedStatementExecutor implements PreparedStatementExecutor {

    @Override
    public void execute(final Connection connection, final String sql) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            execute(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void execute(final PreparedStatement preparedStatement) throws SQLException;

}
