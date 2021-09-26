package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementValueSetter {

    public static void setPreparedStatementValues(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    public static void setPreparedStatementValues(PreparedStatement pstmt, PreparedStatementSetter pss) throws SQLException {
        pss.setValues(pstmt);
    }

    public static void setPreparedStatementValues(PreparedStatement pstmt, Object[] args, int[] argTypes) throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            pstmt.setObject(i, args[i], argTypes[i]);
        }
    }
}
