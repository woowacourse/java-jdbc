package com.interface21.jdbc.core;

@FunctionalInterface
public interface JdbcOperations {
    int update(String sql, Object... params);
}
