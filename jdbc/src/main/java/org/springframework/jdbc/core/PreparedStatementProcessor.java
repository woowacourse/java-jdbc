package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementProcessor<T> {

    T process(final PreparedStatement preparedStatement) throws SQLException;
}
