package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementMaker {
    PreparedStatement makePreparedStatement(Connection conn) throws SQLException;
}
