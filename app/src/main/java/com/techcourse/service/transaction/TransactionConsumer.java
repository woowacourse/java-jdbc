package com.techcourse.service.transaction;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionConsumer {

    void execute(Connection connection) throws SQLException;
}
