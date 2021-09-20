package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    private PreparedStatementSetter() {}

    public static void setValues(PreparedStatement pstmt, Object... objects) throws SQLException {
        int i = 1;
        for (Object object : objects) {
            pstmt.setObject(i++, object);
        }
    }
}
