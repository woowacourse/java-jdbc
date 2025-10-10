package com.interface21.jdbc.core.execution.command;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteExecution implements CommandExecution {

    @Override
    public void execute(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.executeUpdate();
    }
}
