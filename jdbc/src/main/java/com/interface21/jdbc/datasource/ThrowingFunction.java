package com.interface21.jdbc.datasource;

public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
