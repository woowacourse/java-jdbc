package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallBack<T> {

    T callback(PreparedStatement preparedStatement) throws SQLException;
}
