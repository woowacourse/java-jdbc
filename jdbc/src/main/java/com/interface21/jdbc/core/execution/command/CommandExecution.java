package com.interface21.jdbc.core.execution.command;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface CommandExecution {

    int execute(PreparedStatement preparedStatement) throws SQLException;
}
