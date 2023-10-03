package org.springframework.jdbc.core;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface PreparedStatementExecutor<T> {

    T execute(final PreparedStatement preparedStatement);
}
