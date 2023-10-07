package org.springframework.transaction;

import java.sql.Connection;

@FunctionalInterface
public interface ConnectionAction<T> {

    T execute(Connection connection);
}
