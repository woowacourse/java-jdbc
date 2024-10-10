package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementProcessor<T> {
    T process(PreparedStatement ps) throws SQLException;
}
