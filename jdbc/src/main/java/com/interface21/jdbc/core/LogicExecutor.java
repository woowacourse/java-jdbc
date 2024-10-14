package com.interface21.jdbc.core;

import java.sql.Connection;
import java.util.function.Function;

@FunctionalInterface
public interface LogicExecutor<T> extends Function<Connection, T> {
}
