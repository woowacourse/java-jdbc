package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryExecution {

    Object execute(PreparedStatement preparedStatement) throws SQLException;
}
