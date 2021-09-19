package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LongSetter implements PreparedStatementSetter {

    @Override
    public void set(PreparedStatement pstmt, int index, Object arg) throws SQLException {
        if (arg instanceof Long) {
            pstmt.setLong(index, (Long) arg);
        }
    }
}
