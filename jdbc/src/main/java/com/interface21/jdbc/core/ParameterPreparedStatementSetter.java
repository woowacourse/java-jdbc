package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ParameterPreparedStatementSetter implements PreparedStatementSetter {

    private final List<Object> parameters;

    public ParameterPreparedStatementSetter(Object[] parameters) {
        this.parameters = Arrays.asList(parameters);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object parameter = parameters.get(index);
            preparedStatement.setObject(toParameterIndex(index), parameter);
        }
    }

    private int toParameterIndex(int index) {
        return index + 1;
    }
}
