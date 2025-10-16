package com.interface21.jdbc.core.execution.command;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateExecution implements CommandExecution {

    @Override
    public int execute(PreparedStatement preparedStatement) throws SQLException {
        int rowCount = preparedStatement.executeUpdate();
        if (rowCount != 1) {
            throw new SQLException("Insert operation failed, affected rows: " + rowCount);
        }
        return rowCount;
    }
}
