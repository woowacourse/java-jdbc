package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface PreparedStatementProcessor<T> {

    T process(PreparedStatement preparedStatement) throws SQLException;
}
