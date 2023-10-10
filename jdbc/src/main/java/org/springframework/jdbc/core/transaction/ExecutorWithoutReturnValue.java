package org.springframework.jdbc.core.transaction;

import java.sql.SQLException;

@FunctionalInterface
public interface ExecutorWithoutReturnValue {

    void execute() throws SQLException;
}
