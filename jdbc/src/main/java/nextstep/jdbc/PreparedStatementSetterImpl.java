package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetterImpl implements PreparedStatementSetter {

    private final Object[] args;

    public PreparedStatementSetterImpl(final Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(final PreparedStatement ps) throws SQLException {
        for (int idx = 0; idx < args.length; idx++) {
            ps.setObject(idx + 1, args[idx]);
        }
    }
}
