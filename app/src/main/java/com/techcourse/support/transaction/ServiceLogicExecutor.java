package com.techcourse.support.transaction;

import java.sql.SQLException;

@FunctionalInterface
public interface ServiceLogicExecutor {

    void execute() throws SQLException;
}
