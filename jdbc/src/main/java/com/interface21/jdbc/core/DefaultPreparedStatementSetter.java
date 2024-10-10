package com.interface21.jdbc.core;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DefaultPreparedStatementSetter implements PreparedStatementSetter {

    private final List<SQLParameter> sqlParameters;

    public DefaultPreparedStatementSetter(Object... values) {
        sqlParameters = new ArrayList<>();
        for(int i = 0; i < values.length; i++) {
            sqlParameters.add(new SQLParameter(i + PARAMETER_START_INDEX, values[i], JDBCType.JAVA_OBJECT));
        }
    }

    public DefaultPreparedStatementSetter(SQLParameter... sqlParameters) {
        this.sqlParameters = List.of(sqlParameters);
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        validateParameterCount(preparedStatement);
        for (SQLParameter sqlParameter : sqlParameters) {
            setValue(preparedStatement, sqlParameter);
        }
    }

    private void validateParameterCount(PreparedStatement preparedStatement) throws SQLException {
        if(sqlParameters.size() != preparedStatement.getParameterMetaData().getParameterCount()) {
            throw new SQLException("Invalid number of parameters");
        }
    }

    private void setValue(PreparedStatement preparedStatement, SQLParameter sqlParameter) throws SQLException {
        if(sqlParameter.type() == JDBCType.JAVA_OBJECT) {
            preparedStatement.setObject(sqlParameter.parameterIndex(), sqlParameter.value());
            return;
        }
        preparedStatement.setObject(sqlParameter.parameterIndex(), sqlParameter.value(), sqlParameter.type());
    }
}
