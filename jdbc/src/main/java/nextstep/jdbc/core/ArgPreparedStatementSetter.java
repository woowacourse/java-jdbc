package nextstep.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgPreparedStatementSetter(final Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(final PreparedStatement ps) throws SQLException {
        int argIdx = 0;
        for (Object arg : args) {
            argIdx++;
            if (arg instanceof String) {
                ps.setString(argIdx, (String) arg);
                continue;
            }
            if (arg instanceof Long) {
                ps.setLong(argIdx, (Long) arg);
                continue;
            }
            if (arg instanceof Integer) {
                ps.setLong(argIdx, (Integer) arg);
            }
        }
    }
}
