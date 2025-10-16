package com.interface21.jdbc.core.execution.command;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteExecution implements CommandExecution {

    @Override
    public int execute(PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeUpdate();
    }
}
