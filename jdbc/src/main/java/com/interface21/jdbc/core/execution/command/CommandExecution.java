package com.interface21.jdbc.core.execution.command;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface CommandExecution {

    void execute(PreparedStatement preparedStatement) throws SQLException;
}
