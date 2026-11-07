package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Binds parameters without executing or closing the statement. */
@FunctionalInterface
public interface PreparedStatementSetter {

    void setValues(PreparedStatement statement) throws SQLException;
}
