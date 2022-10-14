package nextstep.jdbc.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementUtils {

    private StatementUtils() {
    }

    public static void setArguments(final PreparedStatement pstmt, final Object... args) throws SQLException {
        int parameterIndex = 1;
        for (Object arg : args) {
            pstmt.setObject(parameterIndex++, arg);
        }
    }
}
