package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementUtil {

    public static void setValues(PreparedStatement preparedStatement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
