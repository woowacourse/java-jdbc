package nextstep.jdbc.execution.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentsSetter {

    public static void setArguments(PreparedStatement statement, Object[] arguments) throws SQLException {
        if (arguments == null) {
            return;
        }
        for (int index = 0; index < arguments.length; index++) {
            statement.setObject(index + 1, arguments[index]);
        }
    }
}
