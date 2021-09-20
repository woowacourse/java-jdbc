package nextstep.jdbc.parametervalue;

import java.sql.PreparedStatement;

public abstract class ParameterValue {

    private final Object value;

    protected ParameterValue(Object value) {
        this.value = value;
    }

    public abstract void setPreparedStatementParameter(int parameterIndex, PreparedStatement preparedStatement);

    public final Object getValue() {
        return value;
    }
}
