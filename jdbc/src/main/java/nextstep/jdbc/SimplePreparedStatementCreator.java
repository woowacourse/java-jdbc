package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimplePreparedStatementCreator implements PreparedStatementCreator {

    @Override
    public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
        return null;
    }
}
