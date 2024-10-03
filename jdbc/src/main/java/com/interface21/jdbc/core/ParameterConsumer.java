package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ParameterConsumer {
    void apply(PreparedStatement preparedStatement, int parameterIndex, Object parameter) throws SQLException;
}
