package com.interface21.jdbc.core;

import java.sql.Connection;

@FunctionalInterface
public interface ConnectionCallback {

    void execute(Connection connection);
}
