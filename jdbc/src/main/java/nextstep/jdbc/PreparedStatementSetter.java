package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;


public class PreparedStatementSetter{

    private final Object[] args;

    public PreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        if (Objects.isNull(args)) {
            throw new IllegalStateException("must initialize args");
        }

        for (int row = 0; row < args.length; row++) {
            preparedStatement.setObject(row + 1, args[row]);
        }
    }
}
