package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class ArgumentPreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(final Object[] args) {
        this.args = args;
    }

    public void setValues(PreparedStatement ps) throws SQLException {
        if (Objects.nonNull(args)) {
            for (int i = 0; i < args.length; i++) {
                doSetValue(ps, i + 1, this.args[i]);
            }
        }
    }

    private void doSetValue(final PreparedStatement ps, final int parameterPosition, final Object argValue) throws SQLException {
        StatementCreatorUtils.setParameterValue(ps, parameterPosition, argValue);
    }
}
