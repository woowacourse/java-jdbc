package nextstep.jdbc.parametervalue;

import nextstep.jdbc.exception.ParameterSetIncorrectlyException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ShortParameterValue extends ParameterValue {

    public ShortParameterValue(Object value) {
        super(value);
    }

    @Override
    public void setPreparedStatementParameter(int parameterIndex, PreparedStatement preparedStatement) {
        try {
            preparedStatement.setShort(parameterIndex, (Short) getValue());
        } catch (SQLException e) {
            throw new ParameterSetIncorrectlyException("Parameter Index : " + parameterIndex + ", Set Short Value Failed!!", e);
        }
    }
}
