package nextstep.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {
    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement pstmt) throws SQLException {
        if (this.args != null) {
            for (int i = 0; i < this.args.length; i++) {
                Object arg = this.args[i];
                pstmt.setObject(i + 1, arg);
            }
        }
    }
}
