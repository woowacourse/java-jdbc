package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    public static PreparedStatement setParameters(final PreparedStatement statement, final Object... args)
            throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            statement.setObject(i, args[i - 1]);
        }
        return statement;
    }
}
