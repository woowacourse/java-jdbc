package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;


public class PreparedStatementSetter{

    private final Object[] args;

    private PreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    public static PreparedStatementSetter from (Object[] args) {
        if (Objects.isNull(args)) {
            throw new IllegalStateException("must initialize args");
        }
        return new PreparedStatementSetter(args);
    }

    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        for (int row = 0; row < args.length; row++) {
            preparedStatement.setObject(row + 1, args[row]);
        }
    }
}
