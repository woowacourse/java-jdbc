package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ParameterSetter {

    public static void setParameter(Object[] objects, PreparedStatement preparedStatement) throws SQLException {
        validateParameterCount(objects, preparedStatement);
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
    }

    private static void validateParameterCount(Object[] objects, PreparedStatement preparedStatement) throws SQLException {
        ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
        if (objects.length != parameterMetaData.getParameterCount()) {
            throw new DataAccessException("파라미터 값의 개수가 올바르지 않습니다");
        }
    }
}
