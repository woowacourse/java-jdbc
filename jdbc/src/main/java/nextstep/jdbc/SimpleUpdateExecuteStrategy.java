package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimpleUpdateExecuteStrategy implements PreparedStatementExecuteStrategy<Integer> {

    @Override
    public Integer extract(final PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeUpdate();
    }
}
