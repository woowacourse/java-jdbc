package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(final Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(final PreparedStatement preparedStatement) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            preparedStatement.setObject(index, arg);
            index++;
        }
    }
}
