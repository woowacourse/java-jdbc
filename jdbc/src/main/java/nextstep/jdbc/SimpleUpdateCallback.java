package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimpleUpdateCallback implements PreparedStatementCallback<Integer> {

    @Override
    public Integer extract(final PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeUpdate();
    }
}
