package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStrategy {

    PreparedStatement createStatement(Connection connection) throws SQLException;
}
