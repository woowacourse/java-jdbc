package com.interface21.transaction;

import java.sql.Connection;

@FunctionalInterface
public interface ConnectionProvider {

    Connection getConnection();
}
