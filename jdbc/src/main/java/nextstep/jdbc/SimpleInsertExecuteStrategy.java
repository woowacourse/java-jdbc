package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleInsertExecuteStrategy implements PreparedStatementExecuteStrategy<Long> {

    @Override
    public Long extract(final PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.executeUpdate();
        final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        }
        return null;
    }
}
