package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimpleStatementExecutor<T> implements StatementExecutor<Integer> {

    @Override
    public Integer execute(final PreparedStatement preparedStatement) {
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
