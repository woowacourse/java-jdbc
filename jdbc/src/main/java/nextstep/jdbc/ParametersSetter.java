package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ParametersSetter implements PreparedStatementSetter{

    private static final int SQL_PARAMETER_START_INDEX = 1;

    private final Object[] parameters;

    public ParametersSetter(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        int index = SQL_PARAMETER_START_INDEX;
        for (Object parameter : parameters) {
            preparedStatement.setObject(index, parameter);
            index++;
        }
    }
}
