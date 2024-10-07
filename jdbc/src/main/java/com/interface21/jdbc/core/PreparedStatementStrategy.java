package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementStrategy {

    PreparedStatement makePreparedStatement(Connection connection) throws SQLException;
}
