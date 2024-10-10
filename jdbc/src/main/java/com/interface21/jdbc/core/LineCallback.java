package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

public interface LineCallback<T> {

    T callback(Connection connection, String sql) throws SQLException;
}
