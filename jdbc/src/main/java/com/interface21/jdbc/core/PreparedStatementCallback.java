package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<R> {

    R callBack(PreparedStatement preparedStatement) throws SQLException;
}
