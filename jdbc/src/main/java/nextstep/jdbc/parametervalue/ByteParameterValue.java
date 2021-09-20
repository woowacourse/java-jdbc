package nextstep.jdbc.parametervalue;

import nextstep.jdbc.exception.ParameterSetIncorrectlyException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ByteParameterValue extends ParameterValue {

    public ByteParameterValue(Object value) {
        super(value);
    }

    @Override
    public void setPreparedStatementParameter(int parameterIndex, PreparedStatement preparedStatement) {
        try {
            preparedStatement.setByte(parameterIndex, (Byte) getValue());
        } catch (SQLException e) {
            throw new ParameterSetIncorrectlyException("Parameter Index : " + parameterIndex + ", Set Byte Value Failed!!", e);
        }
    }
}
