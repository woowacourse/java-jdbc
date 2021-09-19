package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StringSetter implements PreparedStatementSetter {

    @Override
    public void set(PreparedStatement pstmt, int index, Object arg) throws SQLException {
        if (arg instanceof String) {
            pstmt.setString(index, (String) arg);
        }
    }
}
