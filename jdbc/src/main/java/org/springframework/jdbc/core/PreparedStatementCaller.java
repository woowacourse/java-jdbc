package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCaller<T> {

    T call(final PreparedStatement preparedStatement) throws SQLException;
}
