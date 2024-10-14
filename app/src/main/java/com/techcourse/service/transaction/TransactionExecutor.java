package com.techcourse.service.transaction;

import java.sql.SQLException;

public interface TransactionExecutor {

    void execute() throws SQLException;
}
