package com.interface21.jdbc.core;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface QueryExecutor<T> {
    T execute(PreparedStatement preparedStatement) throws SQLException;

    default void setParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            Object param = parameters[i];

            switch (param) {
                case Integer intParam -> preparedStatement.setInt(i + 1, intParam);
                case Long longParam -> preparedStatement.setLong(i + 1, longParam);
                case Double doubleParam -> preparedStatement.setDouble(i + 1, doubleParam);
                case Float floatParam -> preparedStatement.setFloat(i + 1, floatParam);
                case Boolean boolParam -> preparedStatement.setBoolean(i + 1, boolParam);
                case String stringParam -> preparedStatement.setString(i + 1, stringParam);
                case Date dateParam -> preparedStatement.setDate(i + 1, dateParam);
                case null, default -> preparedStatement.setObject(i + 1, param);  // 그 외 타입은 setObject로 처리
            }
        }
    }
}
