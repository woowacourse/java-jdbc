package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        validateParameterCount(args, preparedStatement);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    private void validateParameterCount(Object[] args, PreparedStatement preparedStatement) throws SQLException {
        ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
        if (args.length != parameterMetaData.getParameterCount()) {
            throw new DataAccessException("파라미터 값의 개수가 올바르지 않습니다");
        }
    }
}
