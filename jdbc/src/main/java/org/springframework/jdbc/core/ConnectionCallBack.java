package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionCallBack<T> {
    T doInConnection(Connection con) throws SQLException;
}
