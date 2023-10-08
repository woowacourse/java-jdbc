package org.springframework.transaction.support;

import java.sql.Connection;

@FunctionalInterface
public interface LogicExecutor {

    void run(Connection conn);
}
