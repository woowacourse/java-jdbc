package nextstep.jdbc.parametervalue;

import nextstep.jdbc.exception.ParameterSetIncorrectlyException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DoubleParameterValue extends ParameterValue {

    public DoubleParameterValue(Object value) {
        super(value);
    }

    @Override
    public void setPreparedStatementParameter(int parameterIndex, PreparedStatement preparedStatement) {
        try {
            preparedStatement.setDouble(parameterIndex, (Double) getValue());
        } catch (SQLException e) {
            throw new ParameterSetIncorrectlyException("Parameter Index : " + parameterIndex + ", Set Double Value Failed!!", e);
        }
    }
}
