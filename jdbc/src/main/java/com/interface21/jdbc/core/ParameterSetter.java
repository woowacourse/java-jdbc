package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ParameterSetter {

    void setParameter(int index, PreparedStatement preparedStatement, Object parameter) throws SQLException;
}
