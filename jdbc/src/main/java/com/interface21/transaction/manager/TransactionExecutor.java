package com.interface21.transaction.manager;

import java.sql.SQLException;

public interface TransactionExecutor {

    void execute() throws SQLException;
}
