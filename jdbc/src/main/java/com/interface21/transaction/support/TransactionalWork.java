package com.interface21.transaction.support;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionalWork {
    void execute(Connection connection) throws Exception;
}
