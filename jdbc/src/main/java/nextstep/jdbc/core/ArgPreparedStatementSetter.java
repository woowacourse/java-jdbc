package nextstep.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;

public class ArgPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgPreparedStatementSetter(final Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(final PreparedStatement ps) {
        IntStream.range(0, args.length)
                .forEach(i -> setObjects(ps, i + 1, args[i]));
    }

    private void setObjects(final PreparedStatement ps, final int index, final Object arg) {
        try {
            ps.setObject(index, arg);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
