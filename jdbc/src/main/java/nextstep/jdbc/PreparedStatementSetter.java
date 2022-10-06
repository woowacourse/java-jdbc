package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    private PreparedStatementSetter() {
    }

    public static void setValues(PreparedStatement preparedStatement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            final int parameterIndex = i + 1;
            preparedStatement.setObject(parameterIndex, args[i]);
        }
    }
}
