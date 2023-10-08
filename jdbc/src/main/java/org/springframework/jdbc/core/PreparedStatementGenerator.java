package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementGenerator {

    PreparedStatement generate(final Connection connection) throws SQLException;
}
