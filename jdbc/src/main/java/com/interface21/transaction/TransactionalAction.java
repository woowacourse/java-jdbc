package com.interface21.transaction;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionalAction {

    void execute(Connection connection);
}
