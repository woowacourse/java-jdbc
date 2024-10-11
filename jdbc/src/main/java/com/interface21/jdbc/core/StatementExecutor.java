package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementExecutor<R> {

    R apply(PreparedStatement statement) throws SQLException;
}
