package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface UpdateQueryExecutor {

    void execute(final PreparedStatement preparedStatement) throws SQLException;
}
