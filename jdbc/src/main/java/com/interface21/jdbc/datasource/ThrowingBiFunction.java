package com.interface21.jdbc.datasource;

@FunctionalInterface
public interface ThrowingBiFunction<K, V, R, E extends Exception> {
    R apply(K k, V v) throws E;
}
