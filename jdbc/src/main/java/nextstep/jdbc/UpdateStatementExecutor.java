package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateStatementExecutor<T> implements StatementExecutor<Integer> {

    @Override
    public Integer execute(final PreparedStatement preparedStatement) {
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
