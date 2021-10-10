package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.exception.PreparedStatementSetFailureException;

public class PreparedStatementValueSetter {

    private final PreparedStatement pstmt;

    public PreparedStatementValueSetter(PreparedStatement pstmt) {
        this.pstmt = pstmt;
    }

    public void setPreparedStatementValue(Object arg) {
        try {
            pstmt.setObject(1, arg);
        } catch (SQLException exception) {
            throw new PreparedStatementSetFailureException(exception);
        }
    }

    public void setPreparedStatementValues(Object[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        } catch (SQLException exception) {
            throw new PreparedStatementSetFailureException(exception);
        }
    }
}
