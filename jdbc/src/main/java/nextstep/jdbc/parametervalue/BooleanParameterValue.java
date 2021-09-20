package nextstep.jdbc.parametervalue;

import nextstep.jdbc.exception.ParameterSetIncorrectlyException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BooleanParameterValue extends ParameterValue {

    public BooleanParameterValue(Object value) {
        super(value);
    }

    @Override
    public void setPreparedStatementParameter(int parameterIndex, PreparedStatement preparedStatement) {
        try {
            preparedStatement.setBoolean(parameterIndex, (Boolean) getValue());
        } catch (SQLException e) {
            throw new ParameterSetIncorrectlyException("Parameter Index : " + parameterIndex + ", Set Boolean Value Failed!!", e);
        }
    }
}
