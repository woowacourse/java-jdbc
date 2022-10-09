package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    private PreparedStatementSetter() {
    }

    public static void setParams(PreparedStatement preparedStatement, Object... args) throws SQLException {
        for (var index = 0; index < args.length; index++) {
            preparedStatement.setObject(index + 1, args[index]);
        }
    }
}
