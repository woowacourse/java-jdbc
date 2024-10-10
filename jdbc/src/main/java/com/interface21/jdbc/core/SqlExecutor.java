package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlExecutor<T> {

    T executor(PreparedStatement preparedStatement) throws SQLException;
}
