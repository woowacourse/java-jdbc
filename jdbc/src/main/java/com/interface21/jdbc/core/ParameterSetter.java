package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ParameterSetter {

    void setParameters(PreparedStatement preparedStatement) throws SQLException;
}
