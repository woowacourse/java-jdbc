package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TypeBasedPreparedStatementSetter implements PreparedStatementSetter {

    private final List<SQLParameter> sqlParameters;

    public TypeBasedPreparedStatementSetter(SQLParameter... sqlParameters) {
        this.sqlParameters = List.of(sqlParameters);
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        for (SQLParameter sqlParameter : sqlParameters) {
            preparedStatement.setObject(sqlParameter.parameterIndex(), sqlParameter.value(), sqlParameter.type());
        }
    }
}
