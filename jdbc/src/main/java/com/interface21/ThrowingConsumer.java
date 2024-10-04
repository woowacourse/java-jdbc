package com.interface21;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}
