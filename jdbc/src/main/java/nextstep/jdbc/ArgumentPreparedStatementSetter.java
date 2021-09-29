package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement pstmt) throws SQLException {
        if (isNonNull()) {
            setObjects(pstmt);
        }
    }

    private boolean isNonNull() {
        return args != null;
    }

    private void setObjects(PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
