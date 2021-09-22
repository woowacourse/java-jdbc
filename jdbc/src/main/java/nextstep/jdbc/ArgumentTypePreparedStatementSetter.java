package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentTypePreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentTypePreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    @Override
    public void setValue(PreparedStatement preparedStatement) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            preparedStatement.setObject(index++, arg);
        }
    }

}
