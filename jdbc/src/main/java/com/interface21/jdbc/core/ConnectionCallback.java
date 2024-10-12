package com.interface21.jdbc.core;

import java.sql.Connection;

@FunctionalInterface
public interface ConnectionCallback<T> {

    T doInConnection(Connection conn);
}
