package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface executeQueryCallback<T> {

    T execute(final PreparedStatement preparedStatement) throws SQLException;
}
