package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter{

    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        if (Objects.nonNull(args)) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                doSetValue(ps, i+1, arg);
            }
        }
    }

    private void doSetValue(PreparedStatement ps, int paramPosition, Object arg) throws SQLException {
        ps.setObject(paramPosition, arg);
    }
}
