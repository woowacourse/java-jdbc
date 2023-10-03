package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface QueryExecution {

    Object execute(PreparedStatement preparedStatement) throws SQLException;
}
