package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementFactory {

    private PreparedStatementFactory() {
    }

    public static PreparedStatement create(final Connection conn, final String sql, final Object[] parameters)
            throws SQLException {
        final PreparedStatement statement = conn.prepareStatement(sql);
        setParameters(statement, parameters);

        return statement;
    }

    private static void setParameters(final PreparedStatement statement, final Object[] parameters)
            throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }
}
