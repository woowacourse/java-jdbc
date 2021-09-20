package nextstep.jdbc.setter;

import nextstep.jdbc.parametervalue.ParameterValue;
import nextstep.jdbc.parametervalue.ParameterValueGenerator;

import java.sql.PreparedStatement;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) {
        for (int i = 0; i < args.length; i++) {
            ParameterValue parameterValue = ParameterValueGenerator.createParameterValue(args[i]);
            parameterValue.setPreparedStatementParameter(i + 1, preparedStatement);
        }
    }
}
