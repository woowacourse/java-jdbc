package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter{

    private final Object[] args;

    public ArgumentPreparedStatementSetter(final Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(final PreparedStatement preparedStatement) throws SQLException {
        if (this.args == null) {
            return;
        }
        for (int i = 0; i < this.args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
