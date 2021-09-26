package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.exception.PreparedStatementSetFailureException;

public class PreparedStatementValueSetter {

    private final PreparedStatement pstmt;

    public PreparedStatementValueSetter(PreparedStatement pstmt) {
        this.pstmt = pstmt;
    }

    public void setPreparedStatementValues(Object[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        } catch (SQLException e) {
            throw new PreparedStatementSetFailureException(e.getMessage(), e.getCause());
        }
    }

    public void setPreparedStatementValues(PreparedStatementSetter pss) {
        try {
            pss.setValues(pstmt);
        } catch (SQLException e) {
            throw new PreparedStatementSetFailureException(e.getMessage(), e.getCause());
        }
    }

    public void setPreparedStatementValues(Object[] args, int[] argTypes) {
        try {
            for (int i = 1; i <= args.length; i++) {
                pstmt.setObject(i, args[i], argTypes[i]);
            }
        } catch (SQLException e) {
            throw new PreparedStatementSetFailureException(e.getMessage(), e.getCause());
        }
    }
}
